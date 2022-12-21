package com.malinskiy.marathon.ios

import com.dd.plist.PropertyListParser
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.ScreenRecordingPolicy
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.ios.LifecycleAction
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
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.listener.LineListener
import com.malinskiy.marathon.execution.listener.LogListener
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.ios.cmd.CommandExecutor
import com.malinskiy.marathon.ios.cmd.CommandResult
import com.malinskiy.marathon.ios.cmd.FileBridge
import com.malinskiy.marathon.ios.executor.listener.AppleTestRunListener
import com.malinskiy.marathon.ios.executor.listener.CompositeTestRunListener
import com.malinskiy.marathon.ios.executor.listener.DebugTestRunListener
import com.malinskiy.marathon.ios.executor.listener.ProgressReportingListener
import com.malinskiy.marathon.ios.executor.listener.ResultBundleRunListener
import com.malinskiy.marathon.ios.executor.listener.video.ScreenRecordingListener
import com.malinskiy.marathon.ios.executor.listener.TestResultsListener
import com.malinskiy.marathon.ios.executor.listener.TestRunListenerAdapter
import com.malinskiy.marathon.ios.executor.listener.screenshot.ScreenCapturerTestRunListener
import com.malinskiy.marathon.ios.logparser.XctestEventProducer
import com.malinskiy.marathon.ios.logparser.formatter.TestLogPackageNameFormatter
import com.malinskiy.marathon.ios.logparser.parser.DebugLogPrinter
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureException
import com.malinskiy.marathon.ios.logparser.parser.DiagnosticLogsPathFinder
import com.malinskiy.marathon.ios.logparser.parser.SessionResultsPathFinder
import com.malinskiy.marathon.ios.xcrun.simctl.SimulatorState
import com.malinskiy.marathon.ios.test.TestEvent
import com.malinskiy.marathon.ios.test.TestRequest
import com.malinskiy.marathon.ios.xcrun.Xcrun
import com.malinskiy.marathon.ios.xctestrun.Xctestrun
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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import net.schmizz.sshj.connection.ConnectionException
import net.schmizz.sshj.connection.channel.OpenFailException
import net.schmizz.sshj.transport.TransportException
import java.awt.image.BufferedImage
import java.io.File
import java.time.Duration
import javax.imageio.ImageIO
import kotlin.coroutines.CoroutineContext

class AppleSimulatorDevice(
    override val udid: String,
    private val xcrun: Xcrun,
    private val fileManager: FileManager,
    private val configuration: Configuration,
    private val vendorConfiguration: VendorConfiguration.IOSConfiguration,
    private val commandExecutor: CommandExecutor,
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
        newFixedThreadPoolContext(2, "AppleSimulatorDevice - execution - ${commandExecutor.host.id}")
    }
    override val coroutineContext: CoroutineContext = dispatcher
    override val remoteFileManager: RemoteFileManager = RemoteFileManager(this)
    override val storagePath = "/tmp/marathon"

    /**
     * Called only once per device's lifetime
     */
    override suspend fun setup() {
        val simctlDevices = xcrun.simctl.device.list()
        val simctlDevice = simctlDevices.find { it.udid == udid } ?: throw DeviceSetupException("simulator $udid not found")
        env = fetchEnvvars()

        home = env["HOME"]
            ?: xcrun.simctl.simulator.getenv(udid, "SIMULATOR_HOST_HOME")
                ?: ""
        if (home.isBlank()) {
            throw DeviceSetupException("simulator $udid: invalid value $home for environment variable HOME")
        }
        val logDirectory = simctlDevice.logPath ?: "$home/Library/Developer/CoreSimulator/Devices/$udid"
        logFile = "$logDirectory/system.log"
        rootPath = xcrun.simctl.simulator.getenv(udid, "HOME")?.let {
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
    }

    override suspend fun prepare(configuration: Configuration) {
        async(CoroutineName("prepare $serialNumber")) {
            supervisorScope {
                track.trackDevicePreparing(this@AppleSimulatorDevice) {
                    remoteFileManager.removeRemoteDirectory()
                    remoteFileManager.createRemoteDirectory()
                    //Clean slate for the recorder
                    executeWorkerCommand(listOf("pkill", "-f", "'simctl io ${udid} recordVideo'"))
                    mutableListOf<Deferred<Unit>>().apply {
                        add(async {
                            AppleApplicationInstaller(
                                configuration,
                                vendorConfiguration
                            ).prepareInstallation(this@AppleSimulatorDevice)
                        })
                        add(async {
                            if (vendorConfiguration.lifecycleConfiguration.onPrepare.contains(LifecycleAction.TERMINATE)) {
                                terminateRunningSimulator()
                            }
                            if (vendorConfiguration.lifecycleConfiguration.onPrepare.contains(LifecycleAction.ERASE)) {
                                if (!shutdown()) {
                                    logger.warn("Exception shutting down simulator $udid")
                                } else {
                                    logger.info { "Simulator $udid shutdown" }
                                }
                                if (!erase()) {
                                    logger.warn("Exception erasing simulator $udid")
                                } else {
                                    logger.info { "Erased simulator $udid" }
                                }
                            }
                            disableHardwareKeyboard()
                            if (!boot()) {
                                logger.warn("Exception booting simulator $udid")
                            }
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
        deferred: CompletableDeferred<TestBatchResults>,
        progressReporter: ProgressReporter
    ) {
        try {
            async(CoroutineName("execute $serialNumber")) {
                supervisorScope {
                    var executionLineListeners = setOf<LineListener>()
                    try {
                        val (listener, lineListeners) = createExecutionListeners(devicePoolId, testBatch, deferred, progressReporter)
                        executionLineListeners = lineListeners.onEach { addLineListener(it) }
                        AppleDeviceTestRunner(this@AppleSimulatorDevice).execute(configuration, vendorConfiguration, testBatch, listener)
                    } finally {
                        executionLineListeners.forEach { removeLineListener(it) }
                    }
                }
            }.await()
        } catch (e: ConnectionException) {
            throw DeviceLostException(e)
        } catch (e: TransportException) {
            throw DeviceLostException(e)
        } catch (e: OpenFailException) {
            throw DeviceLostException(e)
        } catch (e: IllegalStateException) {
            throw DeviceLostException(e)
        } catch (e: DeviceFailureException) {
            throw DeviceLostException(e)
        }
    }

    override fun dispose() {
        try {
            commandExecutor.close()
        } catch (e: Exception) {
            logger.debug(e) { "Error closing command executor" }
        }
        dispatcher.close()
    }

    private fun createExecutionListeners(
        devicePoolId: DevicePoolId,
        testBatch: TestBatch,
        deferred: CompletableDeferred<TestBatchResults>,
        progressReporter: ProgressReporter,
    ): Pair<CompositeTestRunListener, Set<LineListener>> {
        val logWriter = LogWriter(fileManager)

        val progressReportingListener = ProgressReportingListener(
            deviceInfo = toDeviceInfo(),
            poolId = devicePoolId,
            progressReporter = progressReporter,
        )

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
            LogListener(toDeviceInfo(), this, devicePoolId, testBatch.id, logWriter)
                .also { attachmentProviders.add(it) }
        )

        val diagnosticLogsPathFinder = DiagnosticLogsPathFinder()
        val sessionResultsPathFinder = SessionResultsPathFinder()
        val debugLogPrinter = DebugLogPrinter(hideRunnerOutput = vendorConfiguration.hideRunnerOutput)

        val logListeners = setOf(
            diagnosticLogsPathFinder,
            sessionResultsPathFinder,
            debugLogPrinter
        )

        return Pair(
            CompositeTestRunListener(
                listOf(
                    TestResultsListener(testBatch, this, deferred, timer, remoteFileManager, xcrun.xcresulttool, attachmentProviders),
                    logListener,
                    progressReportingListener,
                    DebugTestRunListener(this),
                    diagnosticLogsPathFinder,
                    sessionResultsPathFinder,
                    recorderListener,
                    ResultBundleRunListener(this, vendorConfiguration.xcresult, devicePoolId, testBatch, fileManager),
                )
            ), logListeners
        )
    }

    override suspend fun executeTestRequest(request: TestRequest): ReceiveChannel<List<TestEvent>> {
        val xctestrun = Xctestrun(vendorConfiguration.safecxtestrunPath())
        val packageNameFormatter = TestLogPackageNameFormatter(xctestrun.productModuleName, xctestrun.targetName)

        return produce {
            xcrun.xcodebuild.testWithoutBuilding(udid, request).use { session ->
                withContext(Dispatchers.IO) {
                    val deferredStdout = supervisorScope {
                        async {
                            val testEventProducer = XctestEventProducer(packageNameFormatter, timer)
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

    override suspend fun startVideoRecording(remotePath: String): CommandResult? {
        val videoConfiguration = vendorConfiguration.screenRecordConfiguration.videoConfiguration
        val codec = videoConfiguration.codec
        val display = videoConfiguration.display
        val mask = videoConfiguration.mask
        return xcrun.simctl.io.recordVideo(udid, remotePath, codec, display, mask, remoteFileManager.remoteVideoPidfile())
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
        val success = xcrun.simctl.io.screenshot(udid, tempDestination, cfg.type, cfg.display, cfg.mask)
        if (!success) {
            return false
        }

        return pullFile(tempDestination, dst)
    }

    override suspend fun getScreenshot(timeout: Duration): BufferedImage? {
        val tempFile = kotlin.io.path.createTempFile(suffix = ".png").toFile()
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
            SimulatorState.SHUTDOWN -> true
            SimulatorState.CREATING, SimulatorState.BOOTING, SimulatorState.BOOTED -> if (xcrun.simctl.simulator.shutdown(udid)) {
                waitForState(SimulatorState.SHUTDOWN)
            } else {
                false
            }

            SimulatorState.UNKNOWN -> false
        }
    }

    override suspend fun erase(): Boolean {
        when (state()) {
            SimulatorState.CREATING, SimulatorState.BOOTING, SimulatorState.BOOTED -> {
                if (!shutdown()) {
                    return false
                }
            }

            SimulatorState.SHUTDOWN, SimulatorState.UNKNOWN -> Unit
        }
        return xcrun.simctl.simulator.erase(listOf(udid))
    }

    /**
     * @return true if booted
     */
    suspend fun boot(): Boolean {
        return when (state()) {
            SimulatorState.CREATING -> waitForBoot()
            SimulatorState.SHUTDOWN -> {
                if (xcrun.simctl.simulator.boot(udid)) {
                    waitForBoot()
                } else {
                    false
                }
            }

            SimulatorState.BOOTED -> true
            SimulatorState.BOOTING -> {
                waitForBoot()
            }

            SimulatorState.UNKNOWN -> false
        }
    }

    suspend fun monitorStatus(): Boolean {
        return xcrun.simctl.simulator.monitorStatus(udid)
    }

    suspend fun shutdown(udid: String) {
        when (state()) {
            SimulatorState.CREATING -> {
                logger.warn { "simulator $udid: unable to shutdown simulator at the same time as it's created" }
            }

            SimulatorState.BOOTING, SimulatorState.BOOTED -> {
                if (!xcrun.simctl.simulator.shutdown(udid)) {
                    logger.error { "simulator $udid: unable to shutdown simulator" }
                }
            }

            SimulatorState.SHUTDOWN, SimulatorState.UNKNOWN -> Unit
        }
        if (!waitForState(SimulatorState.SHUTDOWN, "Device $udid successfully shutdown!")) {
            logger.error { "simulator $udid: unable to confirm shutdown within timeout" }
        }
    }

    private suspend fun state(): SimulatorState {
        return getDeviceProperty<Int>("state", false)?.toInt()?.let {
            when (it) {
                0 -> SimulatorState.CREATING
                1 -> SimulatorState.SHUTDOWN
                3 -> {
                    //This means device is booted but doesn't mean it's actually ready
                    val running = isRunning()
                    if (running) {
                        SimulatorState.BOOTED
                    } else {
                        SimulatorState.BOOTING
                    }
                }

                else -> SimulatorState.UNKNOWN
            }
        } ?: SimulatorState.UNKNOWN

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
        state: SimulatorState,
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
        return waitForState(SimulatorState.BOOTED, "Device $udid booted!", "Device $udid is still booting...")
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
        return xcrun.simctl.simulator.getenv(udid, key) ?: default
    }

    private suspend fun <T> getDeviceProperty(name: String, cached: Boolean = true): T? {
        if (!cached) {
            fetchDeviceDescriptor()
        }
        return (deviceDescriptor?.get(name) as? T)
    }

    suspend fun isRunning(): Boolean {
        return xcrun.simctl.simulator.isRunning(udid, runtimeVersion)
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

    private val lineListeners = mutableListOf<LineListener>()

    override fun addLineListener(listener: LineListener) {
        synchronized(lineListeners) {
            lineListeners.add(listener)
        }
    }

    override fun removeLineListener(listener: LineListener) {
        synchronized(lineListeners) {
            lineListeners.remove(listener)
        }
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
        var ps = executeWorkerCommand(listOf("sh", "-c", "/bin/ps | /usr/bin/grep $udid"))?.combinedStdout?.trim() ?: ""
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
                logger.trace("Terminated loaded simulators")
            } else {
                logger.debug("Failed to terminate loaded simulators, stdout=${result?.combinedStdout}, stderr=${result?.combinedStderr}")
            }

            ps = executeWorkerCommand(listOf("sh", "-c", "/bin/ps | /usr/bin/grep $udid"))?.combinedStdout?.trim() ?: ""
            if (ps.isNotBlank()) {
                logger.debug { "Terminated loaded simulators, but there are still some processes with simulator udid: ${System.lineSeparator()}$ps" }
            }
        }
    }

    private suspend fun disableHardwareKeyboard() {
        executeWorkerCommand(
            listOf(
                "/usr/libexec/PlistBuddy",
                "-c",
                "'Add :DevicePreferences:$udid:ConnectHardwareKeyboard bool false'",
                "$home/Library/Preferences/com.apple.iphonesimulator.plist",
            )
        )
        val result = executeWorkerCommand(
            listOf(
                "/usr/libexec/PlistBuddy",
                "-c",
                "'Set :DevicePreferences:$udid:ConnectHardwareKeyboard false'",
                "$home/Library/Preferences/com.apple.iphonesimulator.plist",
            )
        )
        if (result?.exitCode == 0) {
            logger.trace("Disabled hardware keyboard for $udid")
        } else {
            logger.debug("Failed to disable hardware keyboard for $udid, stdout=${result?.combinedStdout}")
        }
    }
}
