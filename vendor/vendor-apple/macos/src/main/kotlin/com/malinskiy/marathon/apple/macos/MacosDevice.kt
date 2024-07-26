package com.malinskiy.marathon.apple.macos

import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.apple.AppleApplicationInstaller
import com.malinskiy.marathon.apple.AppleDevice
import com.malinskiy.marathon.apple.AppleDeviceTestRunner
import com.malinskiy.marathon.apple.AppleTestBundleIdentifier
import com.malinskiy.marathon.apple.RemoteFileManager
import com.malinskiy.marathon.apple.bin.AppleBinaryEnvironment
import com.malinskiy.marathon.apple.cmd.CommandExecutor
import com.malinskiy.marathon.apple.cmd.CommandResult
import com.malinskiy.marathon.apple.cmd.FileBridge
import com.malinskiy.marathon.apple.configuration.Transport
import com.malinskiy.marathon.apple.extensions.bundleConfiguration
import com.malinskiy.marathon.apple.listener.AppleTestRunListener
import com.malinskiy.marathon.apple.listener.CompositeTestRunListener
import com.malinskiy.marathon.apple.listener.DebugTestRunListener
import com.malinskiy.marathon.apple.listener.ResultBundleRunListener
import com.malinskiy.marathon.apple.listener.TestResultsListener
import com.malinskiy.marathon.apple.listener.TestRunListenerAdapter
import com.malinskiy.marathon.apple.logparser.XctestEventProducer
import com.malinskiy.marathon.apple.logparser.parser.DeviceFailureException
import com.malinskiy.marathon.apple.logparser.parser.DeviceFailureReason
import com.malinskiy.marathon.apple.logparser.parser.DiagnosticLogsPathFinder
import com.malinskiy.marathon.apple.logparser.parser.SessionResultsPathFinder
import com.malinskiy.marathon.apple.model.AppleTestBundle
import com.malinskiy.marathon.apple.model.Sdk
import com.malinskiy.marathon.apple.model.XcodeVersion
import com.malinskiy.marathon.apple.test.TestEvent
import com.malinskiy.marathon.apple.test.TestRequest
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.apple.macos.Permission
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.device.OperatingSystem
import com.malinskiy.marathon.device.file.measureFileTransfer
import com.malinskiy.marathon.device.file.measureFolderTransfer
import com.malinskiy.marathon.device.screenshot.Rotation
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.exceptions.DeviceLostException
import com.malinskiy.marathon.exceptions.DeviceSetupException
import com.malinskiy.marathon.exceptions.IncompatibleDeviceException
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.listener.LineListener
import com.malinskiy.marathon.execution.listener.LogListener
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.attachment.AttachmentProvider
import com.malinskiy.marathon.report.logs.LogWriter
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.time.Timer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import net.schmizz.sshj.connection.ConnectionException
import net.schmizz.sshj.connection.channel.OpenFailException
import net.schmizz.sshj.transport.TransportException
import java.awt.image.BufferedImage
import java.io.File
import java.time.Duration
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

class MacosDevice(
    override val udid: String,
    override val transport: Transport,
    override var sdk: Sdk,
    override val binaryEnvironment: AppleBinaryEnvironment,
    private val testBundleIdentifier: AppleTestBundleIdentifier,
    val fileManager: FileManager,
    private val configuration: Configuration,
    private val vendorConfiguration: VendorConfiguration.MacosConfiguration,
    internal val commandExecutor: CommandExecutor,
    private val fileBridge: FileBridge,
    private val track: Track,
    private val timer: Timer,
) : AppleDevice, CoroutineScope {
    override val logger = MarathonLogging.logger {}

    override var operatingSystem: OperatingSystem = OperatingSystem("Unknown")
    override val serialNumber: String = "$udid@${commandExecutor.host.id}"
    override var model: String = "Unknown"
    override var manufacturer: String = "Unknown"

    override val networkState: NetworkState
        get() = when (healthy) {
            true -> NetworkState.CONNECTED
            false -> NetworkState.DISCONNECTED
        }
    override var deviceFeatures: Collection<DeviceFeature> = emptyList()
    override val healthy: Boolean = true
    override var abi: String = "Unknown"
    private lateinit var version: String

    override val orientation: Rotation = Rotation.ROTATION_0
    private lateinit var runtimeVersion: String
    private lateinit var runtimeBuildVersion: String
    private lateinit var env: Map<String, String>
    private lateinit var home: String
    private lateinit var logFile: String
    private lateinit var devicePlistPath: String
    private var deviceDescriptor: Map<*, *>? = null
    private val dispatcher by lazy {
        newFixedThreadPoolContext(
            vendorConfiguration.threadingConfiguration.deviceThreads,
            "MacosDevice - execution - ${commandExecutor.host.id}"
        )
    }
    override val coroutineContext: CoroutineContext = dispatcher
    override val remoteFileManager: RemoteFileManager = RemoteFileManager(this)
    override val storagePath = "${AppleDevice.SHARED_PATH}/$udid"
    private lateinit var xcodeVersion: XcodeVersion
    private lateinit var testBundle: AppleTestBundle

    /**
     * Called only once per device's lifetime
     */
    override suspend fun setup() {
        env = fetchEnvvars()

        xcodeVersion = binaryEnvironment.xcrun.xcodebuild.getVersion()

        home = env["HOME"] ?: ""
        if (home.isBlank()) {
            throw DeviceSetupException("macOS $udid: invalid value $home for environment variable HOME")
        }
        val logDirectory = "/private/var/log/"
        logFile = "$logDirectory/system.log"


        model = binaryEnvironment.ioreg.getModel()
        manufacturer = binaryEnvironment.ioreg.getManufacturer()
        operatingSystem = OperatingSystem(binaryEnvironment.swvers.getVersion())
        abi = executeWorkerCommand(listOf("uname", "-m"))?.let {
            if (it.successful) {
                it.combinedStdout.trim()
            } else {
                null
            }
        } ?: "Unknown"

        deviceFeatures = detectFeatures()
    }

    override suspend fun executeTestRequest(request: TestRequest): ReceiveChannel<List<TestEvent>> {
        return produce {
            binaryEnvironment.xcrun.xcodebuild.testWithoutBuilding(udid, sdk, request, vendorConfiguration.xcodebuildTestArgs)
                .use { session ->
                    withContext(Dispatchers.IO) {
                        val deferredStdout = supervisorScope {
                            async {
                                val testEventProducer =
                                    XctestEventProducer(request.testTargetName ?: "", timer)
                                for (line in session.stdout) {
                                    testEventProducer.process(line)?.let {
                                        send(it)
                                    }
                                    lineListeners.forEach { it.onLine(line) }
                                }
                            }
                        }

                        val deferredStderr = supervisorScope {
                            async {
                                for (line in session.stderr) {
                                    if (line.trim().isNotBlank()) {
                                        logger.error { "macOS $udid: stderr=$line" }
                                        lineListeners.forEach { it.onLine(line) }
                                    }
                                }
                            }
                        }

                        deferredStderr.await()
                        deferredStdout.await()
                        val exitCode = session.exitCode.await()
                        // 70 = no devices
                        // 65 = ** TEST EXECUTE FAILED **: crash
                        logger.debug { "Finished test batch execution with exit status $exitCode" }
                        close()
                    }
                }
        }

    }

    override suspend fun executeWorkerCommand(command: List<String>): CommandResult? {
        return commandExecutor.safeExecute(
            command = command,
            timeout = vendorConfiguration.timeoutConfiguration.shell,
            idleTimeout = vendorConfiguration.timeoutConfiguration.shellIdle,
            env = emptyMap(),
            null
        )
    }

    override suspend fun pushFile(src: File, dst: String): Boolean {
        return measureFileTransfer(src) {
            fileBridge.send(src, dst)
        }
    }

    override suspend fun pullFile(src: String, dst: File): Boolean {
        return measureFileTransfer(dst) {
            fileBridge.receive(src, dst)
        }
    }

    override suspend fun pushFolder(src: File, dst: String): Boolean {
        return measureFolderTransfer(src) {
            fileBridge.send(src, dst)
        }
    }

    override suspend fun pullFolder(src: String, dst: File): Boolean {
        return measureFolderTransfer(dst) {
            fileBridge.receive(src, dst)
        }
    }

    override suspend fun install(remotePath: String): Boolean {
        return true
    }

    override suspend fun getScreenshot(timeout: Duration, dst: File): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getScreenshot(timeout: Duration): BufferedImage? {
        TODO("Not yet implemented")
    }

    override suspend fun startVideoRecording(remotePath: String): CommandResult? {
        TODO("Not yet implemented")
    }

    override suspend fun stopVideoRecording(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun shutdown(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun erase(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun prepare(configuration: Configuration) {
        async(CoroutineName("prepare $serialNumber")) {
            supervisorScope {
                track.trackDevicePreparing(this@MacosDevice) {
                    remoteFileManager.removeRemoteDirectory()
                    remoteFileManager.createRemoteDirectory()
                    remoteFileManager.createRemoteSharedDirectory()
                    mutableListOf<Deferred<Unit>>().apply {
                        add(async {
                            MacosApplicationInstaller(
                                vendorConfiguration,
                            ).prepareInstallation(this@MacosDevice)
                            testBundle = vendorConfiguration.bundleConfiguration()?.let {
                                val xctest = it.xctest
                                val app = it.app
                                val testApp = it.testApp
                                AppleTestBundle(app, testApp, xctest, sdk)
                            } ?: throw IllegalArgumentException("No test bundle provided")
                        })
                    }.awaitAll()
                }
            }
        }.await()
    }

    override suspend fun execute(
        configuration: Configuration,
        devicePoolId: DevicePoolId,
        testBatch: TestBatch,
        deferred: CompletableDeferred<TestBatchResults>
    ) {
        var job: Job? = null
        try {
            job = async(coroutineContext + CoroutineName("execute $serialNumber")) {
                supervisorScope {
                    var executionLineListeners = setOf<LineListener>()
                    try {
                        val (listener, lineListeners) = createExecutionListeners(devicePoolId, testBatch, deferred)
                        executionLineListeners = lineListeners.onEach { addLineListener(it) }
                        AppleDeviceTestRunner(this@MacosDevice, testBundleIdentifier).execute(
                            configuration,
                            testBundle,
                            testBatch,
                            listener
                        )
                    } finally {
                        executionLineListeners.forEach { removeLineListener(it) }
                    }
                }
            }
            job.await()
        } catch (e: ConnectionException) {
            throw DeviceLostException(e)
        } catch (e: TransportException) {
            throw DeviceLostException(e)
        } catch (e: OpenFailException) {
            throw DeviceLostException(e)
        } catch (e: IllegalStateException) {
            throw DeviceLostException(e)
        } catch (e: DeviceFailureException) {
            when (e.reason) {
                DeviceFailureReason.IncompatibleDevice -> throw IncompatibleDeviceException(
                    e
                )

                else -> throw DeviceLostException(e)
            }
        } catch(e: CancellationException) {
            job?.cancel(e)
            throw e
        }
    }

    override fun dispose() {
        dispatcher.close()
    }

    private suspend fun fetchEnvvars(): Map<String, String> {
        val commandResult = commandExecutor.safeExecute(
            command = listOf("env"),
            timeout = vendorConfiguration.timeoutConfiguration.shell,
            idleTimeout = vendorConfiguration.timeoutConfiguration.shellIdle,
            env = emptyMap(),
            workdir = null
        )
        if (commandResult?.successful != true) {
            throw DeviceSetupException("macOS $udid: unable to detect environment variables")
        }

        return commandResult.stdout
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .associate {
                val key = it.substringBefore('=').trim()
                val value = it.substringAfter('=').trim()
                Pair(key, value)
            }
    }

    private suspend fun detectFeatures(): List<DeviceFeature> {
        return emptyList()
    }

    private val lineListeners = CopyOnWriteArrayList<LineListener>()

    override fun addLineListener(listener: LineListener) {
        lineListeners.add(listener)
    }

    override fun removeLineListener(listener: LineListener) {
        lineListeners.remove(listener)
    }

    private fun createExecutionListeners(
        devicePoolId: DevicePoolId,
        testBatch: TestBatch,
        deferred: CompletableDeferred<TestBatchResults>,
    ): Pair<CompositeTestRunListener, Set<LineListener>> {
        val logWriter = LogWriter(fileManager)

        val attachmentProviders = mutableListOf<AttachmentProvider>()
        val recorderListener = object : AppleTestRunListener {}

        val logListener = TestRunListenerAdapter(
            LogListener(toDeviceInfo(), this, devicePoolId, testBatch.id, logWriter, attachmentName = "xcodebuild-log")
                .also { attachmentProviders.add(it) }
        )

        val diagnosticLogsPathFinder = DiagnosticLogsPathFinder()
        val sessionResultsPathFinder = SessionResultsPathFinder()
        val debugLogPrinter =
            com.malinskiy.marathon.apple.logparser.parser.DebugLogPrinter(hideRunnerOutput = vendorConfiguration.hideRunnerOutput)

        val logListeners = setOf(
            diagnosticLogsPathFinder,
            sessionResultsPathFinder,
            debugLogPrinter
        )

        return Pair(
            CompositeTestRunListener(
                listOf(
                    TestResultsListener(
                        testBatch,
                        this,
                        devicePoolId,
                        deferred,
                        timer,
                        remoteFileManager,
                        binaryEnvironment.xcrun.xcresulttool,
                        attachmentProviders
                    ),
                    logListener,
                    DebugTestRunListener(this),
                    diagnosticLogsPathFinder,
                    sessionResultsPathFinder,
                    recorderListener,
                    ResultBundleRunListener(this, vendorConfiguration.xcresult, devicePoolId, testBatch, fileManager),
                )
            ), logListeners
        )
    }

    suspend fun grant(client: String, permission: Set<Permission>) {
        if (operatingSystem.major?.let { it > 10 } != true) {
            throw IncompatibleDeviceException("Modifying permissions is supported to macOS 10+")
        }

        val epoch = System.currentTimeMillis() / 1000
        permission.forEach {
            val query =
                "replace into access (service,client,client_type,auth_value,auth_reason,auth_version,indirect_object_identifier,flags,last_modified) values (\"${it.value}\",\"${client}\",0,2,1,1,\"UNUSED\",0,$epoch);"
            binaryEnvironment.sqlite3.query("/Library/Application Support/com.apple.TCC/TCC.db", query, sudo = true)
            binaryEnvironment.sqlite3.query("/Users/${env["USER"]}/Library/Application Support/com.apple.TCC/TCC.db", query, sudo = true)
        }
    }
}
