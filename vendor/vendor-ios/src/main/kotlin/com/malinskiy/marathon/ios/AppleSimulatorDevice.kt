package com.malinskiy.marathon.ios

import com.dd.plist.PropertyListParser
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.device.OperatingSystem
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.exceptions.DeviceSetupException
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.listener.LineListener
import com.malinskiy.marathon.execution.listener.LogListener
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.ios.cmd.CommandExecutor
import com.malinskiy.marathon.ios.cmd.CommandResult
import com.malinskiy.marathon.ios.cmd.FileBridge
import com.malinskiy.marathon.ios.cmd.remote.CommandSession
import com.malinskiy.marathon.ios.cmd.remote.SshjCommandUnresponsiveException
import com.malinskiy.marathon.ios.executor.listener.AppleTestRunListener
import com.malinskiy.marathon.ios.executor.listener.CompositeTestRunListener
import com.malinskiy.marathon.ios.executor.listener.ResultBundleRunListener
import com.malinskiy.marathon.ios.executor.listener.TestRunListenerAdapter
import com.malinskiy.marathon.ios.logparser.XCTestLogParser
import com.malinskiy.marathon.ios.logparser.formatter.TestLogPackageNameFormatter
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureException
import com.malinskiy.marathon.ios.simctl.Simctl
import com.malinskiy.marathon.ios.simctl.SimulatorState
import com.malinskiy.marathon.ios.test.TestEvent
import com.malinskiy.marathon.ios.test.TestRequest
import com.malinskiy.marathon.ios.xctestrun.Xctestrun
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.withContext
import net.schmizz.sshj.connection.ConnectionException
import net.schmizz.sshj.connection.channel.OpenFailException
import net.schmizz.sshj.transport.TransportException
import java.io.File
import java.time.Duration
import java.util.concurrent.TimeoutException
import kotlin.coroutines.CoroutineContext

class AppleSimulatorDevice(
    override val udid: String,
    private val simctl: Simctl,
    private val fileManager: FileManager,
    private val configuration: Configuration,
    private val vendorConfiguration: VendorConfiguration.IOSConfiguration,
    private val commandExecutor: CommandExecutor,
    private val fileBridge: FileBridge,
) : AppleDevice, CoroutineScope {
    private val logger = MarathonLogging.logger {}

    override var operatingSystem: OperatingSystem = OperatingSystem("Unknown")
    override val serialNumber: String = "$udid@${commandExecutor.workerId}"
    override var model: String = "Unknown"
    override var manufacturer: String = "Unknown"
    override val networkState: NetworkState = NetworkState.CONNECTED
    override var deviceFeatures: Collection<DeviceFeature> = emptyList()
    override val healthy: Boolean = true
    override var abi: String = "Unknown"
    private lateinit var version: String

    private lateinit var runtimeVersion: String
    private lateinit var runtimeBuildVersion: String
    private lateinit var deviceType: String
    private lateinit var env: Map<String, String>
    private lateinit var home: String
    private lateinit var logFile: String
    private lateinit var rootPath: String
    private lateinit var devicePlistPath: String
    private var deviceDescriptor: Map<*, *>? = null
    private val deviceContext = newFixedThreadPoolContext(1, serialNumber)

    override val coroutineContext: CoroutineContext
        get() = deviceContext + Job()

    /**
     * Called only once per device's lifetime
     */
    override suspend fun setup() {
        val simctlDevices = simctl.list()
        val simctlDevice = simctlDevices.find { it.udid == udid } ?: throw DeviceSetupException("simulator $udid not found")
        env = fetchEnvvars()

        home = env["HOME"]
            ?: simctl.getenv(udid, "SIMULATOR_HOST_HOME")
                ?: ""
        if (home.isBlank()) {
            throw DeviceSetupException("simulator $udid: invalid value $home for environment variable HOME")
        }
        val logDirectory = simctlDevice.logPath ?: "$home/Library/Developer/CoreSimulator/Devices/$udid"
        logFile = "$logDirectory/system.log"
        rootPath = simctl.getenv(udid, "HOME")?.let {
            if (it.endsWith("/data")) {
                it.substringBefore("/data")
            } else {
                it
            }
        } ?: "$home/Library/Developer/CoreSimulator/Devices/$udid"

        devicePlistPath = "$rootPath/device.plist"
        fetchDeviceDescriptor()

        deviceType = simctlDevice.deviceTypeIdentifier
            ?: getDeviceProperty("deviceType")
                ?: throw DeviceSetupException("simulator $udid: unable to detect deviceType")

        model = getSimpleEnvProperty("SIMULATOR_MODEL_IDENTIFIER", deviceType)
        manufacturer = "Apple"
        runtimeBuildVersion = getSimpleEnvProperty("SIMULATOR_RUNTIME_BUILD_VERSION")
        runtimeVersion = getSimpleEnvProperty("SIMULATOR_RUNTIME_VERSION")
        version = getSimpleEnvProperty("SIMULATOR_VERSION_INFO")
        abi = executeWorkerCommand("uname -m").let {
            if (it.exitCode == 0) {
                it.stdout.trim()
            } else {
                "Unknown"
            }
        }
        
        deviceFeatures = detectFeatures()
    }

    override suspend fun prepare(configuration: Configuration) = Unit

    override suspend fun execute(
        configuration: Configuration,
        devicePoolId: DevicePoolId,
        testBatch: TestBatch,
        deferred: CompletableDeferred<TestBatchResults>,
        progressReporter: ProgressReporter
    ) = withContext(coroutineContext + CoroutineName("execute")) {
        val fileManager = FileManager(configuration.outputConfiguration.maxPath, configuration.outputDir)
        val executionListeners = createExecutionListeners(devicePoolId, testBatch, fileManager)

        val testRunner = AppleDeviceTestRunner(this@AppleSimulatorDevice)
        testRunner.execute(
            configuration,
            vendorConfiguration,
            testBatch,
            executionListeners,
        )
    }

    override fun dispose() {
        deviceContext.close()
    }

    private fun createExecutionListeners(
        devicePoolId: DevicePoolId,
        testBatch: TestBatch,
        fileManager: FileManager,
    ): AppleTestRunListener {
        return CompositeTestRunListener(
            listOf(
                ResultBundleRunListener(this, vendorConfiguration.xcresult, devicePoolId, testBatch, fileManager)
            )
        )
    }

    override suspend fun executeTestRequest(request: TestRequest): ReceiveChannel<List<TestEvent>> {
        val xctestrun = Xctestrun(vendorConfiguration.safecxtestrunPath())
        val packageNameFormatter = TestLogPackageNameFormatter(xctestrun.productModuleName, xctestrun.targetName)
        val logParser = XCTestLogParser(
            this,
            packageNameFormatter,
            devicePoolId,
            rawTestBatch,
            deferred,
            progressReporter,
            vendorConfiguration.hideRunnerOutput,
            timer
        )

        val logListener = TestRunListenerAdapter(LogListener(toDeviceInfo(), this, devicePoolId, logWriter))

        val command =
            listOf(
                "cd '${request.workdir}';",
                "NSUnbufferedIO=YES",
                "xcodebuild test-without-building",
                "-xctestrun ${request.xctestrun}",
                request.toXcodebuildTestFilter(),
                "-resultBundlePath ${request.xcresult}",
                "-destination 'platform=iOS simulator,id=$udid' ;",
                "exit"
            )
                .joinToString(" ")
                .also { logger.debug("\u001b[1m$it\u001b[0m") }

        val exitStatus = try {
            commandExecutor.execInto(
                command,
                Duration.ofMillis(configuration.testBatchTimeoutMillis),
                Duration.ofMillis(configuration.testOutputTimeoutMillis),
                logParser::onLine
            )
        } catch (e: SshjCommandUnresponsiveException) {
            logger.error("No output from remote shell")
            disconnectAndThrow(e)
        } catch (e: TimeoutException) {
            logger.error("Connection timeout")
            disconnectAndThrow(e)
        } catch (e: ConnectionException) {
            logger.error("ConnectionException")
            disconnectAndThrow(e)
        } catch (e: TransportException) {
            logger.error("TransportException")
            disconnectAndThrow(e)
        } catch (e: OpenFailException) {
            logger.error("Unable to open session")
            disconnectAndThrow(e)
        } catch (e: IllegalStateException) {
            logger.error("Unable to start a new SSH session. Client is disconnected")
            disconnectAndThrow(e)
        } catch (e: DeviceFailureException) {
            logger.error("Execution failed because ${e.reason}")
            failureReason = e.reason
            disconnectAndThrow(e)
        } finally {
            logParser.close()
            executionListeners.afterTestRun()

            if (!healthy) {
                logger.debug("Last log before device termination")
                logger.debug(logParser.getLastLog())
            }

            if (logParser.diagnosticLogPaths.isNotEmpty())
                logger.info("Diagnostic logs available at ${logParser.diagnosticLogPaths}")

            if (logParser.sessionResultPaths.isNotEmpty())
                logger.info("Session results available at ${logParser.sessionResultPaths}")
        }

        // 70 = no devices
        // 65 = ** TEST EXECUTE FAILED **: crash
        logger.debug("Finished test batch execution with exit status $exitStatus")
    }

    override suspend fun executeWorkerCommand(command: String): CommandResult {
        TODO("Not yet implemented")
    }

    override suspend fun pushFile(src: File, dst: String): Boolean {
        return fileBridge.send(src, dst)
    }

    override suspend fun pullFile(src: String, dst: File): Boolean {
        return fileBridge.receive(src, dst)
    }

    override suspend fun pushFolder(src: File, dst: String): Boolean {
        return fileBridge.send(src, dst)
    }

    override suspend fun pullFolder(src: String, dst: File): Boolean {
        return fileBridge.receive(src, dst)
    }

    override suspend fun startVideoRecording(remotePath: String): CommandSession {
        return simctl.recordVideo(udid, remotePath)
    }

    override suspend fun getScreenshot(timeout: Duration, dst: File): Boolean {
        val cfg = vendorConfiguration.screenRecordConfiguration.screenshotConfiguration
        val tempDestination = RemoteFileManager.remoteScreenshot(udid, cfg.type)
        val success = simctl.screenshot(udid, tempDestination, cfg.type, cfg.display, cfg.mask)
        if (!success) {
            return false
        }

        return pullFile(tempDestination, dst)
    }

    override suspend fun shutdown(): Boolean {
        return if (simctl.shutdown(udid)) {
            waitForState(SimulatorState.SHUTDOWN)
        } else {
            false
        }
    }

    override suspend fun erase(): Boolean {
        return simctl.erase(listOf(udid))
    }

    /**
     * @return true if booted
     */
    suspend fun boot(): Boolean {
        return when (state()) {
            SimulatorState.CREATING -> waitForBoot()
            SimulatorState.SHUTDOWN -> {
                if (simctl.boot(udid)) {
                    waitForBoot()
                } else {
                    false
                }
            }

            SimulatorState.BOOTED -> true
            SimulatorState.UNKNOWN -> false
        }
    }

    /**
     * Monitors existing boot process or starts a new boot sequence
     * @return true if booted
     */
    suspend fun bootSync(): Boolean {
        val commandResult = commandExecutor.execBlocking("xcrun simctl bootstatus $udid -b")
        return commandResult.exitCode == 0
    }

    suspend fun shutdown(udid: String) {
        when (state()) {
            SimulatorState.CREATING -> {
                logger.warn { "simulator $udid: unable to shutdown simulator at the same time as it's created" }
            }

            SimulatorState.BOOTED -> {
                if (!simctl.shutdown(udid)) {
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
        return getDeviceProperty("state", false)?.toInt()?.let {
            when (it) {
                0 -> SimulatorState.CREATING
                1 -> SimulatorState.SHUTDOWN
                3 -> SimulatorState.BOOTED
                else -> SimulatorState.UNKNOWN
            }
        } ?: SimulatorState.UNKNOWN

    }

    private suspend fun detectFeatures(): List<DeviceFeature> {
        return enumValues<DeviceFeature>().filter { feature ->
            when(feature) {
                DeviceFeature.VIDEO -> {
                    val commandResult = commandExecutor.execBlocking(
                        "/usr/sbin/system_profiler -detailLevel mini -xml SPDisplaysDataType"
                    )
                    commandResult.exitCode == 0 && commandResult.stdout.contains("spdisplays_metalfeatureset")
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

    private fun fetchDeviceDescriptor() {
        deviceDescriptor = readTextfile(devicePlistPath)?.let { plist ->
            val nsObject = PropertyListParser.parse(plist.toByteArray())
            nsObject.toJavaObject() as Map<*, *>
        }
    }

    private fun readTextfile(path: String): String? {
        val commandResult = commandExecutor.execBlocking("cat $path")
        if (commandResult.exitCode != 0) {
            return null
        }
        return commandResult.stdout
    }

    private suspend fun getSimpleEnvProperty(key: String, default: String = "Unknown"): String {
        return simctl.getenv(udid, key) ?: default
    }

    private suspend fun getDeviceProperty(name: String, cached: Boolean = true): String? {
        if (!cached) {
            fetchDeviceDescriptor()
        }
        return (deviceDescriptor?.get(name) as? String)?.trim()
    }

//    fun isRunning(udid: String): Boolean {
//        val output = exec("spawn $udid launchctl print system | grep com.apple.springboard.services").stdout
//        return output.contains("M   A   com.apple.springboard.services")
//    }

    private fun fetchEnvvars(): Map<String, String> {
        val commandResult = commandExecutor.execBlocking("env")
        if (commandResult.exitCode != 0) {
            throw DeviceSetupException("simulator $udid: unable to detect environment variables")
        }

        return commandResult.stdout
            .lines()
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
}
