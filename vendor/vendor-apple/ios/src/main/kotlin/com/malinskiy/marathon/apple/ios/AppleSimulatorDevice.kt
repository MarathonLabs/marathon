package com.malinskiy.marathon.apple.ios

import com.dd.plist.PropertyListParser
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.apple.AppleDevice
import com.malinskiy.marathon.apple.AppleDeviceTestRunner
import com.malinskiy.marathon.apple.AppleTestBundleIdentifier
import com.malinskiy.marathon.apple.RemoteFileManager
import com.malinskiy.marathon.apple.bin.AppleBinaryEnvironment
import com.malinskiy.marathon.apple.bin.xcrun.simctl.service.ApplicationService
import com.malinskiy.marathon.apple.cmd.CommandExecutor
import com.malinskiy.marathon.apple.cmd.CommandResult
import com.malinskiy.marathon.apple.cmd.FileBridge
import com.malinskiy.marathon.apple.configuration.Transport
import com.malinskiy.marathon.apple.extensions.bundleConfiguration
import com.malinskiy.marathon.apple.ios.listener.DataContainerClearListener
import com.malinskiy.marathon.apple.ios.listener.log.DeviceLogListener
import com.malinskiy.marathon.apple.listener.AppleTestRunListener
import com.malinskiy.marathon.apple.listener.CompositeTestRunListener
import com.malinskiy.marathon.apple.listener.DebugTestRunListener
import com.malinskiy.marathon.apple.listener.ResultBundleRunListener
import com.malinskiy.marathon.apple.listener.TestResultsListener
import com.malinskiy.marathon.apple.listener.TestRunListenerAdapter
import com.malinskiy.marathon.apple.ios.listener.screenshot.ScreenCapturerTestRunListener
import com.malinskiy.marathon.apple.ios.listener.video.ScreenRecordingListener
import com.malinskiy.marathon.apple.model.Sdk
import com.malinskiy.marathon.apple.ios.model.Simulator
import com.malinskiy.marathon.apple.logparser.parser.DeviceFailureException
import com.malinskiy.marathon.apple.logparser.parser.DeviceFailureReason
import com.malinskiy.marathon.apple.logparser.parser.DiagnosticLogsPathFinder
import com.malinskiy.marathon.apple.logparser.parser.SessionResultsPathFinder
import com.malinskiy.marathon.apple.model.AppleTestBundle
import com.malinskiy.marathon.apple.model.XcodeVersion
import com.malinskiy.marathon.apple.test.TestEvent
import com.malinskiy.marathon.apple.test.TestRequest
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.ScreenRecordingPolicy
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.apple.ios.GrantLifecycle
import com.malinskiy.marathon.config.vendor.apple.ios.LifecycleAction
import com.malinskiy.marathon.config.vendor.apple.ios.Permission
import com.malinskiy.marathon.config.vendor.apple.ios.Type
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
import com.malinskiy.marathon.execution.Attachment
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.listener.LineListener
import com.malinskiy.marathon.execution.listener.LogListener
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.attachment.AttachmentProvider
import com.malinskiy.marathon.report.logs.LogWriter
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.time.Timer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import net.schmizz.sshj.connection.ConnectionException
import net.schmizz.sshj.connection.channel.OpenFailException
import net.schmizz.sshj.transport.TransportException
import java.awt.image.BufferedImage
import java.io.File
import java.time.Duration
import java.util.concurrent.CopyOnWriteArrayList
import javax.imageio.ImageIO
import kotlin.coroutines.CoroutineContext

class AppleSimulatorDevice(
    override val udid: String,
    override val transport: Transport,
    override var sdk: Sdk,
    override val binaryEnvironment: AppleBinaryEnvironment,
    private val testBundleIdentifier: AppleTestBundleIdentifier,
    val fileManager: FileManager,
    private val configuration: Configuration,
    private val vendorConfiguration: VendorConfiguration.IOSConfiguration,
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
    private lateinit var deviceType: String
    private lateinit var env: Map<String, String>
    private lateinit var home: String
    private lateinit var logFile: String
    private lateinit var rootPath: String
    private lateinit var devicePlistPath: String
    private var deviceDescriptor: Map<*, *>? = null
    private val dispatcher by lazy {
        newFixedThreadPoolContext(
            vendorConfiguration.threadingConfiguration.deviceThreads,
            "AppleSimulatorDevice - execution - ${commandExecutor.host.id}"
        )
    }
    override val coroutineContext: CoroutineContext = dispatcher
    override val remoteFileManager: RemoteFileManager = RemoteFileManager(this)
    override val storagePath = "${AppleDevice.SHARED_PATH}/$udid"
    private lateinit var xcodeVersion: XcodeVersion
    private lateinit var testBundle: AppleTestBundle
    private var supportsTranscoding: Boolean = false

    /**
     * Called only once per device's lifetime
     */
    override suspend fun setup() {
        withContext(coroutineContext) {
            val simctlDevices = binaryEnvironment.xcrun.simctl.device.listDevices()
            val simctlDevice = simctlDevices.find { it.udid == udid } ?: throw DeviceSetupException("simulator $udid not found")
            env = fetchEnvvars()

            xcodeVersion = binaryEnvironment.xcrun.xcodebuild.getVersion()

            home = env["HOME"]
                ?: binaryEnvironment.xcrun.simctl.simulator.getenv(udid, "SIMULATOR_HOST_HOME")
                    ?: ""
            if (home.isBlank()) {
                throw DeviceSetupException("simulator $udid: invalid value $home for environment variable HOME")
            }
            val logDirectory = simctlDevice.logPath ?: "$home/Library/Developer/CoreSimulator/Devices/$udid"
            logFile = "$logDirectory/system.log"
            rootPath = binaryEnvironment.xcrun.simctl.simulator.getenv(udid, "HOME")?.let {
                if (it.endsWith("/data")) {
                    it.substringBefore("/data")
                } else {
                    it
                }
            } ?: "$home/Library/Developer/CoreSimulator/Devices/$udid"

            devicePlistPath = "$rootPath/device.plist"
            fetchDeviceDescriptor()

            deviceType = simctlDevice.deviceTypeIdentifier
                ?: getDeviceProperty<String>("deviceType")?.trim()
                    ?: throw DeviceSetupException("simulator $udid: unable to detect deviceType")

            model = getSimpleEnvProperty("SIMULATOR_MODEL_IDENTIFIER", deviceType)
            manufacturer = "Apple"
            runtimeBuildVersion = getSimpleEnvProperty("SIMULATOR_RUNTIME_BUILD_VERSION")
            runtimeVersion = getSimpleEnvProperty("SIMULATOR_RUNTIME_VERSION")
            version = getSimpleEnvProperty("SIMULATOR_VERSION_INFO")
            abi = executeWorkerCommand(listOf("uname", "-m"))?.let {
                if (it.successful) {
                    it.combinedStdout.trim()
                } else {
                    null
                }
            } ?: "Unknown"

            deviceFeatures = detectFeatures()

            val transcodingVideoConfiguration = vendorConfiguration.screenRecordConfiguration.videoConfiguration.transcoding
            supportsTranscoding = transcodingVideoConfiguration.enabled && executeWorkerCommand(listOf(transcodingVideoConfiguration.binary, "-version"))?.let {
                if (it.successful) {
                    true
                } else {
                    false
                }
            } ?: false
        }
    }

    override suspend fun prepare(configuration: Configuration) {
        async(coroutineContext + CoroutineName("prepare $serialNumber")) {
            supervisorScope {
                track.trackDevicePreparing(this@AppleSimulatorDevice) {
                    remoteFileManager.removeRemoteDirectory()
                    remoteFileManager.createRemoteDirectory()
                    remoteFileManager.createRemoteSharedDirectory()
                    //Clean slate for the recorder
                    executeWorkerCommand(listOf("pkill", "-f", "'simctl io ${udid} recordVideo'"))
                    mutableListOf<Deferred<Unit>>().apply {
                        add(async {
                            AppleSimulatorApplicationInstaller(
                                vendorConfiguration,
                            ).prepareInstallation(this@AppleSimulatorDevice)
                            testBundle = vendorConfiguration.bundleConfiguration()?.let {
                                val xctest = it.xctest
                                val app = it.app
                                val testApp = it.testApp
                                AppleTestBundle(app, testApp, xctest, sdk)
                            } ?: throw IllegalArgumentException("No test bundle provided")
                        })
                        add(async {
                            handleLifecycle(vendorConfiguration.lifecycleConfiguration.onPrepare)
                            disableHardwareKeyboard()
                            if (!boot()) {
                                logger.warn("Exception booting simulator $udid")
                            }
                            AppleSimulatorMediaImporter(vendorConfiguration)
                                .importMedia(this@AppleSimulatorDevice)
                        })
                    }.awaitAll()
                }
            }
        }.await()
    }

    private suspend fun handleLifecycle(actions: Set<LifecycleAction>) {
        if (actions.contains(LifecycleAction.TERMINATE)) {
            terminateRunningSimulator()
        }
        if (actions.contains(LifecycleAction.SHUTDOWN) || actions.contains(LifecycleAction.ERASE)) {
            if (!shutdown()) {
                logger.warn("Exception shutting down simulator $udid")
            } else {
                logger.info { "Simulator $udid shutdown" }
            }
        }
        if (actions.contains(LifecycleAction.ERASE)) {
            if (!erase()) {
                logger.warn("Exception erasing simulator $udid")
            } else {
                logger.info { "Erased simulator $udid" }
            }
        }
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
                        if (vendorConfiguration.permissions.lifecycle == GrantLifecycle.BEFORE_EACH_BATCH) {
                            for (permission in vendorConfiguration.permissions.grant) {
                                grant(permission, testBundle.appId)
                            }
                        }
                        AppleDeviceTestRunner(this@AppleSimulatorDevice, testBundleIdentifier).execute(
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
                DeviceFailureReason.InvalidSimulatorIdentifier, DeviceFailureReason.IncompatibleDevice -> throw IncompatibleDeviceException(
                    e
                )

                else -> throw DeviceLostException(e)
            }
        } catch (e: CancellationException) {
            job?.cancel(e)
            throw e
        }
    }

    override fun dispose() {
        try {
            runBlocking {
                withContext(NonCancellable) {
                    if (commandExecutor.connected) {
                        handleLifecycle(vendorConfiguration.lifecycleConfiguration.onDispose)
                    }
                }
            }
        } catch (e: Exception) {
            logger.debug(e) { "Error closing command executor" }
        }
        dispatcher.close()
    }

    private fun createExecutionListeners(
        devicePoolId: DevicePoolId,
        testBatch: TestBatch,
        deferred: CompletableDeferred<TestBatchResults>,
    ): Pair<CompositeTestRunListener, Set<LineListener>> {
        val logWriter = LogWriter(fileManager)

        val attachmentProviders = mutableListOf<AttachmentProvider>()
        val recorderListener =
            RecorderTypeSelector.selectRecorderType(deviceFeatures, vendorConfiguration.screenRecordConfiguration)?.let { feature ->
                prepareRecorderListener(
                    feature,
                    fileManager,
                    devicePoolId,
                    testBatch.id,
                    configuration.screenRecordingPolicy,
                    attachmentProviders
                )
            } ?: object : AppleTestRunListener {}

        val logListener = TestRunListenerAdapter(
            LogListener(toDeviceInfo(), this, devicePoolId, testBatch.id, logWriter, attachmentName = Attachment.Name.XCODEBUILDLOG)
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
                    DeviceLogListener(this, vendorConfiguration.deviceLog, devicePoolId, testBatch),
                    DebugTestRunListener(this),
                    diagnosticLogsPathFinder,
                    sessionResultsPathFinder,
                    recorderListener,
                    ResultBundleRunListener(this, vendorConfiguration.xcresult, devicePoolId, testBatch, fileManager),
                    DataContainerClearListener(this, vendorConfiguration.dataContainerClear, testBatch, testBundleIdentifier)
                )
            ), logListeners
        )
    }

    override suspend fun executeTestRequest(request: TestRequest): ReceiveChannel<List<TestEvent>> {
        return produce {
            binaryEnvironment.xcrun.xcodebuild.testWithoutBuilding(udid, sdk, request, vendorConfiguration.xcodebuildTestArgs)
                .use { session ->
                    withContext(Dispatchers.IO) {
                        val deferredStdout = supervisorScope {
                            async {
                                val testEventProducer =
                                    com.malinskiy.marathon.apple.logparser.XctestEventProducer(request.testTargetName ?: "", timer)
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
                                        logger.error { "simulator $udid: stderr=$line" }
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
        return binaryEnvironment.xcrun.simctl.application.install(udid, remotePath).successful
    }

    suspend fun importMedia(remotePath: String): Boolean {
        return binaryEnvironment.xcrun.simctl.mediaService.addMedia(udid, remotePath).successful
    }

    override suspend fun startVideoRecording(remotePath: String): CommandResult? {
        val videoConfiguration = vendorConfiguration.screenRecordConfiguration.videoConfiguration
        val codec = videoConfiguration.codec
        val display = videoConfiguration.display
        val mask = videoConfiguration.mask
        return binaryEnvironment.xcrun.simctl.io.recordVideo(udid, remotePath, codec, display, mask, remoteFileManager.remoteVideoPidfile())
    }

    override suspend fun stopVideoRecording(): Boolean {
        val pidfile = remoteFileManager.remoteVideoPidfile()
        val interruptResult =
            executeWorkerCommand(listOf("sh", "-c", "kill -s INT $(cat $pidfile) && lsof -p $(cat $pidfile) +r 1 &>/dev/null"))
        if (interruptResult?.successful != true) {
            logger.warn { "error while killing recording process, stdout=${interruptResult?.combinedStdout} stderr=${interruptResult?.combinedStderr}, exitCode=${interruptResult?.exitCode}" }
            return false
        }
        remoteFileManager.removeRemotePath(pidfile)
        return true
    }

    override suspend fun getScreenshot(timeout: Duration, dst: File): Boolean {
        val cfg = vendorConfiguration.screenRecordConfiguration.screenshotConfiguration
        val tempDestination = remoteFileManager.remoteScreenshot(udid, cfg.type)
        val success = binaryEnvironment.xcrun.simctl.io.screenshot(udid, tempDestination, cfg.type, cfg.display, cfg.mask)
        if (!success) {
            return false
        }

        return pullFile(tempDestination, dst)
    }

    override suspend fun getScreenshot(timeout: Duration): BufferedImage? {
        val extension = when (vendorConfiguration.screenRecordConfiguration.screenshotConfiguration.type) {
            Type.PNG -> ".png"
            Type.TIFF -> ".tiff"
            Type.BMP -> ".bmp"
            Type.JPEG -> ".jpeg"
        }
        val tempFile = kotlin.io.path.createTempFile(suffix = extension).toFile()
        try {
            if (getScreenshot(timeout, tempFile)) {
                return ImageIO.read(tempFile)
            }
        } finally {
            tempFile.delete()
        }
        return null
    }

    override suspend fun shutdown(): Boolean {
        val state = state()
        return when (state) {
            Simulator.State.SHUTDOWN -> true
            Simulator.State.CREATING, Simulator.State.BOOTING, Simulator.State.BOOTED -> if (binaryEnvironment.xcrun.simctl.simulator.shutdown(
                    udid
                )
            ) {
                waitForState(Simulator.State.SHUTDOWN)
            } else {
                false
            }

            Simulator.State.UNKNOWN -> false
        }
    }

    override suspend fun erase(): Boolean {
        when (state()) {
            Simulator.State.CREATING, Simulator.State.BOOTING, Simulator.State.BOOTED -> {
                if (!shutdown()) {
                    return false
                }
            }

            Simulator.State.SHUTDOWN, Simulator.State.UNKNOWN -> Unit
        }
        return binaryEnvironment.xcrun.simctl.simulator.erase(listOf(udid))
    }

    /**
     * @return true if booted
     */
    suspend fun boot(): Boolean {
        return when (state()) {
            Simulator.State.CREATING -> waitForBoot()
            Simulator.State.SHUTDOWN -> {
                if (binaryEnvironment.xcrun.simctl.simulator.boot(udid)) {
                    waitForBoot()
                } else {
                    false
                }
            }

            Simulator.State.BOOTED -> true
            Simulator.State.BOOTING -> {
                waitForBoot()
            }

            Simulator.State.UNKNOWN -> false
        }
    }

    suspend fun monitorStatus(): Boolean {
        return binaryEnvironment.xcrun.simctl.simulator.monitorStatus(udid)
    }

    suspend fun shutdown(udid: String) {
        when (state()) {
            Simulator.State.CREATING -> {
                logger.warn { "simulator $udid: unable to shutdown simulator at the same time as it's created" }
            }

            Simulator.State.BOOTING, Simulator.State.BOOTED -> {
                if (!binaryEnvironment.xcrun.simctl.simulator.shutdown(udid)) {
                    logger.error { "simulator $udid: unable to shutdown simulator" }
                }
            }

            Simulator.State.SHUTDOWN, Simulator.State.UNKNOWN -> Unit
        }
        if (!waitForState(Simulator.State.SHUTDOWN, "Device $udid successfully shutdown!")) {
            logger.error { "simulator $udid: unable to confirm shutdown within timeout" }
        }
    }

    private suspend fun state(): Simulator.State {
        return getDeviceProperty<Int>("state", false)?.toInt()?.let {
            when (it) {
                0 -> Simulator.State.CREATING
                1 -> Simulator.State.SHUTDOWN
                3 -> {
                    //This means device is booted but doesn't mean it's actually ready
                    val running = isRunning()
                    if (running) {
                        Simulator.State.BOOTED
                    } else {
                        Simulator.State.BOOTING
                    }
                }

                else -> Simulator.State.UNKNOWN
            }
        } ?: Simulator.State.UNKNOWN

    }

    private suspend fun detectFeatures(): List<DeviceFeature> {
        return enumValues<DeviceFeature>().filter { feature ->
            when (feature) {
                DeviceFeature.VIDEO -> {
                    val commandResult = commandExecutor.safeExecute(
                        command = listOf("/usr/sbin/system_profiler", "-detailLevel", "mini", "-xml", "SPDisplaysDataType"),
                        timeout = vendorConfiguration.timeoutConfiguration.shell,
                        idleTimeout = vendorConfiguration.timeoutConfiguration.shellIdle,
                        env = emptyMap(),
                        workdir = null
                    )
                    commandResult?.successful?.let { commandResult.stdout.any { it.contains("spdisplays_metalfeatureset") || it.contains("spdisplays_metalfamily") } }
                        ?: false
                }

                DeviceFeature.SCREENSHOT -> {
                    true
                }
            }
        }
    }

    private suspend fun waitForState(
        state: Simulator.State,
        successMessage: String? = null,
        progressMessage: String? = null,
        timeout: Duration = Duration.ofSeconds(30),
        retries: Int = 30,
    ): Boolean {
        var success = false
        for (i in 1..retries) {
            if (state() == state) {
                successMessage?.let { logger.debug { it } }
                success = true
                break
            } else {
                delay(timeout.toMillis() / retries)
                progressMessage?.let { logger.debug { it } }
            }
        }

        return success
    }

    private suspend fun waitForBoot(): Boolean {
        return waitForState(Simulator.State.BOOTED, "Device $udid booted!", "Device $udid is still booting...")
    }

    private suspend fun fetchDeviceDescriptor() {
        deviceDescriptor = readTextfile(devicePlistPath)?.let { plist ->
            val nsObject = PropertyListParser.parse(plist.toByteArray())
            nsObject.toJavaObject() as Map<*, *>
        }
    }

    suspend fun readTextfile(path: String): String? {
        val commandResult = commandExecutor.safeExecute(
            command = listOf("cat", path),
            timeout = vendorConfiguration.timeoutConfiguration.shell,
            idleTimeout = vendorConfiguration.timeoutConfiguration.shellIdle,
            env = emptyMap(),
            workdir = null
        )
        if (commandResult?.successful != true) {
            return null
        }
        return commandResult.combinedStdout
    }

    private suspend fun getSimpleEnvProperty(key: String, default: String = "Unknown"): String {
        return binaryEnvironment.xcrun.simctl.simulator.getenv(udid, key) ?: default
    }

    private suspend fun <T> getDeviceProperty(name: String, cached: Boolean = true): T? {
        if (!cached) {
            fetchDeviceDescriptor()
        }
        return (deviceDescriptor?.get(name) as? T)
    }

    suspend fun isRunning(): Boolean {
        return binaryEnvironment.xcrun.simctl.simulator.isRunning(udid, runtimeVersion)
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
            throw DeviceSetupException("simulator $udid: unable to detect environment variables")
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

    private val lineListeners = CopyOnWriteArrayList<LineListener>()

    override fun addLineListener(listener: LineListener) {
        lineListeners.add(listener)
    }

    override fun removeLineListener(listener: LineListener) {
        lineListeners.remove(listener)
    }

    private fun prepareRecorderListener(
        feature: DeviceFeature, fileManager: FileManager, devicePoolId: DevicePoolId, testBatchId: String,
        screenRecordingPolicy: ScreenRecordingPolicy,
        attachmentProviders: MutableList<AttachmentProvider>
    ): AppleTestRunListener =
        when (feature) {
            DeviceFeature.VIDEO -> {
                ScreenRecordingListener(
                    fileManager,
                    remoteFileManager,
                    devicePoolId,
                    testBatchId,
                    this,
                    screenRecordingPolicy,
                    vendorConfiguration.screenRecordConfiguration.videoConfiguration,
supportsTranscoding,
                    this,
                )
                    .also { attachmentProviders.add(it) }
            }

            DeviceFeature.SCREENSHOT -> {
                ScreenCapturerTestRunListener(
                    fileManager,
                    devicePoolId,
                    testBatchId,
                    this,
                    screenRecordingPolicy,
                    vendorConfiguration.screenRecordConfiguration.screenshotConfiguration,
                    vendorConfiguration.timeoutConfiguration.screenshot,
                    this
                )
                    .also { attachmentProviders.add(it) }
            }
        }

    private suspend fun terminateRunningSimulator() {
        var ps = executeWorkerCommand(listOf("sh", "-c", "ps aux | grep $udid | grep -v grep"))?.combinedStdout?.trim() ?: ""
        if (ps.isNotBlank()) {
            val result = executeWorkerCommand(
                listOf(
                    "/usr/bin/pkill",
                    "-9",
                    "-l",
                    "-f",
                    udid,
                )
            )
            if (result?.successful == true) {
                logger.debug { "Terminated loaded simulators" }
            } else {
                logger.warn { "Failed to terminate loaded simulators, stdout=${result?.combinedStdout}, stderr=${result?.combinedStderr}" }
            }

            ps = executeWorkerCommand(listOf("sh", "-c", "ps aux | grep $udid | grep -v grep"))?.combinedStdout?.trim() ?: ""
            if (ps.isNotBlank()) {
                logger.debug { "Terminated loaded simulators, but there are still some processes with simulator udid: ${System.lineSeparator()}$ps" }
            }
        }
    }

    private suspend fun disableHardwareKeyboard() {
        binaryEnvironment.plistBuddy.add(
            "$home/Library/Preferences/com.apple.iphonesimulator.plist",
            ":DevicePreferences:$udid:ConnectHardwareKeyboard",
            "bool",
            "false"
        )
        binaryEnvironment.plistBuddy.set(
            "$home/Library/Preferences/com.apple.iphonesimulator.plist",
            ":DevicePreferences:$udid:ConnectHardwareKeyboard",
            "false"
        )
        logger.debug { "Disabled hardware keyboard for $udid" }
    }

    suspend fun grant(permission: Permission, bundleId: String): Boolean {
        return when (permission) {
            Permission.UserTracking -> {
                //This might fail on different versions of iOS runtime. Tested on 17.2
                val query =
                    "replace into access (service, client, client_type, auth_value, auth_reason, auth_version, flags) values ('${permission.value}','$bundleId',0,2,2,1,0);"
                binaryEnvironment.sqlite3.query(
                    "${env["HOME"]}/Library/Developer/CoreSimulator/Devices/$udid/data/Library/TCC/TCC.db",
                    query
                )
                true
            }

            else -> binaryEnvironment.xcrun.simctl.privacy.grant(udid, permission, bundleId).successful
        }
    }

    suspend fun clearAppContainer(bundleId: String) {
        binaryEnvironment.xcrun.simctl.application.terminateApplication(udid, bundleId)

        val containerPath = binaryEnvironment.xcrun.simctl.application.containerPath(udid, bundleId, ApplicationService.ContainerType.DATA)
        if (containerPath.successful) {
            remoteFileManager.removeRemotePath(containerPath.combinedStdout.trim())
        } else {
            logger.warn { "Failed to clear app container:\nstdout: ${containerPath.combinedStdout}\nstderr: ${containerPath.combinedStderr}" }
        }
    }
}
