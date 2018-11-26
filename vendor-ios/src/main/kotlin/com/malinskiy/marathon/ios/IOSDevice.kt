package com.malinskiy.marathon.ios


import com.google.gson.Gson
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.device.OperatingSystem
import com.malinskiy.marathon.exceptions.TestBatchExecutionException
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.ios.cmd.remote.CommandExecutor
import com.malinskiy.marathon.ios.cmd.remote.SshjCommandExecutor
import com.malinskiy.marathon.ios.device.RemoteSimulatorFeatureProvider
import com.malinskiy.marathon.ios.logparser.IOSDeviceLogParser
import com.malinskiy.marathon.ios.logparser.formatter.TestLogPackageNameFormatter
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureException
import com.malinskiy.marathon.ios.simctl.Simctl
import com.malinskiy.marathon.ios.xctestrun.Xctestrun
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.experimental.*
import net.schmizz.sshj.transport.TransportException
import java.io.File
import java.io.InterruptedIOException
import kotlin.coroutines.experimental.coroutineContext

private const val HOSTNAME = "localhost"

class IOSDevice(val udid: String,
                val hostCommandExecutor: CommandExecutor,
                val gson: Gson) : Device {
    val logger = MarathonLogging.logger(udid)
    val simctl = Simctl()

    val name: String?
    private val runtime: String?
    private val deviceType: String?

    init {
        val device = simctl.list(this, gson).find { it.udid == udid }
        runtime = device?.runtime
        name = device?.name
        deviceType = simctl.deviceType(this)
    }

    override val operatingSystem: OperatingSystem
        get() = OperatingSystem(runtime ?: "Unknown")
    override val serialNumber: String
        get() = udid
    override val model: String
        get() = deviceType ?: "Unknown"
    override val manufacturer: String
        get() = "Apple"
    override val networkState: NetworkState
        get() = NetworkState.CONNECTED
    override val deviceFeatures: Collection<DeviceFeature> by lazy {
        RemoteSimulatorFeatureProvider.deviceFeatures(this)
    }
    override var healthy: Boolean = true
    override val abi: String
        get() = "Simulator"

    override fun execute(configuration: Configuration,
                         devicePoolId: DevicePoolId,
                         testBatch: TestBatch,
                         deferred: CompletableDeferred<TestBatchResults>,
                         progressReporter: ProgressReporter) {

        if (!healthy) {
            logger.error("Device $udid seems to be having issues running")
            throw TestBatchExecutionException("Device $udid seems to be failing")
        }

        val iosConfiguration = configuration.vendorConfiguration as IOSConfiguration
        val fileManager = FileManager(configuration.outputDir)

        val remoteXcresultPath = RemoteFileManager.remoteXcresultFile(this)
        val remoteXctestrunFile = RemoteFileManager.remoteXctestrunFile(this)
        val remoteDir = remoteXctestrunFile.parent

        logger.debug { "remote xctestrun = $remoteXctestrunFile" }

        val xctestrun = Xctestrun(iosConfiguration.xctestrunPath)
        val packageNameFormatter = TestLogPackageNameFormatter(xctestrun.productModuleName, xctestrun.targetName)

        logger.debug("tests = ${testBatch.tests.toList()}")

        val logParser = IOSDeviceLogParser(this,
            packageNameFormatter,
            devicePoolId,
            testBatch,
            deferred,
            progressReporter
        )

        val command = listOf("cd '$remoteDir' &&",
            "NSUnbufferedIO=YES",
            "xcodebuild 2>&1 test-without-building",
            "-xctestrun ${remoteXctestrunFile.path}",
            // "-resultBundlePath ${remoteXcresultPath.canonicalPath} ",
            testBatch.toXcodebuildArguments(),
            "-destination 'platform=iOS simulator,id=$udid' ;",
            "exit")
                .joinToString(" ")
                .also { logger.debug(it) }

        val exitStatus = try {
            hostCommandExecutor.exec(command, configuration.testOutputTimeoutMillis, logParser::onLine)
        } catch (e: TimeoutCancellationException) {
            throw TestBatchExecutionException(e)
        } catch(e: DeviceFailureException) {
            logger.error("$e")
            healthy = false
            throw TestBatchExecutionException(e)
        } catch (e: InterruptedIOException) {
            logger.info("InterruptedIOException")
            0
        } catch (e: TransportException) {
            logger.error("TransportException $e, cause ${e.cause}")
            throw TestBatchExecutionException(e)
        }

        // 70 = no devices
        // 65 = ** TEST EXECUTE FAILED **: crash
        logger.debug("finished test batch execution with exit status $exitStatus")
        logParser.close()
    }

    override fun prepare(configuration: Configuration) {
        RemoteFileManager.createRemoteDirectory(this)

        val sshjCommandExecutor = hostCommandExecutor as SshjCommandExecutor
        val derivedDataManager = DerivedDataManager(configuration)

        val remoteXctestrunFile = RemoteFileManager.remoteXctestrunFile(this)
        val xctestrunFile = prepareXctestrunFile(derivedDataManager, remoteXctestrunFile)

        derivedDataManager.sendSynchronized(
            localPath = xctestrunFile,
            remotePath = remoteXctestrunFile.absolutePath,
            hostName = sshjCommandExecutor.hostAddress.hostName,
            port = sshjCommandExecutor.port
        )

        derivedDataManager.sendSynchronized(
            localPath = derivedDataManager.productsDir,
            remotePath = RemoteFileManager.remoteDirectory(this).path,
            hostName = sshjCommandExecutor.hostAddress.hostName,
            port = sshjCommandExecutor.port
        )
    }

    private fun prepareXctestrunFile(derivedDataManager: DerivedDataManager, remoteXctestrunFile: File): File {
        val remotePort = RemoteSimulatorFeatureProvider.availablePort(this)
                .also { logger.debug("Using TCP port $it on device $udid") }

        val xctestrun = Xctestrun(derivedDataManager.xctestrunFile)
        xctestrun.environment("TEST_HTTP_SERVER_PORT", "$remotePort")

        return derivedDataManager.xctestrunFile.
                resolveSibling(remoteXctestrunFile.name)
                .also { it.writeBytes(xctestrun.toXMLByteArray()) }
    }

    override fun dispose() {
        hostCommandExecutor.disconnect()
    }

    override fun toString(): String {
        return "IOSDevice"
    }
}

private fun TestBatch.toXcodebuildArguments(): String = tests
        .map { "-only-testing:\"${it.pkg}/${it.clazz}/${it.method}\"" }
        .joinToString(separator = " ")
