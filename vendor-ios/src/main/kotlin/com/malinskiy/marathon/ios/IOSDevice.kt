package com.malinskiy.marathon.ios


import com.google.gson.Gson
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.device.OperatingSystem
import com.malinskiy.marathon.exceptions.DeviceLostException
import com.malinskiy.marathon.exceptions.TestBatchExecutionException
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.ios.cmd.remote.CommandExecutor
import com.malinskiy.marathon.ios.cmd.remote.SshjCommandExecutor
import com.malinskiy.marathon.ios.cmd.remote.SshjCommandUnresponsiveException
import com.malinskiy.marathon.ios.device.RemoteSimulatorFeatureProvider
import com.malinskiy.marathon.ios.logparser.IOSDeviceLogParser
import com.malinskiy.marathon.ios.logparser.formatter.TestLogPackageNameFormatter
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureException
import com.malinskiy.marathon.ios.simctl.Simctl
import com.malinskiy.marathon.ios.xctestrun.Xctestrun
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import kotlinx.coroutines.experimental.withContext
import kotlinx.coroutines.experimental.launch
import net.schmizz.sshj.transport.TransportException
import java.io.File
import java.util.concurrent.TimeoutException

import kotlin.coroutines.experimental.AbstractCoroutineContextElement
import kotlin.coroutines.experimental.CoroutineContext

private const val HOSTNAME = "localhost"

class IOSDevice(val udid: String,
                val hostCommandExecutor: CommandExecutor,
                val gson: Gson) : Device {


    val logger = MarathonLogging.logger(IOSDevice::class.java.name)
    val simctl = Simctl()

    val name: String?
    private val runtime: String?
    private val deviceType: String?

    private val deviceContext: CoroutineContext = newFixedThreadPoolContext(1, udid)

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

    override suspend fun execute(configuration: Configuration,
                                 devicePoolId: DevicePoolId,
                                 testBatch: TestBatch,
                                 deferred: CompletableDeferred<TestBatchResults>,
                                 progressReporter: ProgressReporter) {

        if (!healthy) {
            logger.error("Device $udid is unhealthy")
            throw TestBatchExecutionException("Device $udid is unhealthy")
        }

        launch(deviceContext) {
            val iosConfiguration = configuration.vendorConfiguration as IOSConfiguration
            val fileManager = FileManager(configuration.outputDir)

            val remoteXctestrunFile = RemoteFileManager.remoteXctestrunFile(this@IOSDevice)
            val remoteDir = remoteXctestrunFile.parent

            logger.debug("Remote xctestrun = $remoteXctestrunFile")

            val xctestrun = Xctestrun(iosConfiguration.xctestrunPath)
            val packageNameFormatter = TestLogPackageNameFormatter(xctestrun.productModuleName, xctestrun.targetName)

            logger.debug("Tests = ${testBatch.tests.toList()}")

            val logParser = IOSDeviceLogParser(this@IOSDevice,
                packageNameFormatter,
                devicePoolId,
                testBatch,
                deferred,
                progressReporter
            )

            val command =
                    listOf("cd '$remoteDir' &&",
                            "NSUnbufferedIO=YES",
                            "xcodebuild test-without-building",
                            "-xctestrun ${remoteXctestrunFile.path}",
                            // "-resultBundlePath ${remoteXcresultPath.canonicalPath} ",
                            testBatch.toXcodebuildArguments(),
                            "-destination 'platform=iOS simulator,id=$udid' ;",
                            "exit")
                            .joinToString(" ")
                            .also { logger.debug("\u001b[1m$it\u001b[0m") }

            val exitStatus = try {
                withContext(deviceContext) {
                    hostCommandExecutor.exec(
                        command,
                        configuration.testOutputTimeoutMillis,
                        logParser::onLine
                    )
                }
            } catch (e: SshjCommandUnresponsiveException) {
                logger.error("No output from remote shell")
                throw TestBatchExecutionException(e)
            } catch (e: TimeoutException) {
                logger.error("Connection timeout")
                throw TestBatchExecutionException(e)
            } catch (e: TransportException) {
                logger.error("TransportException $e, cause ${e.cause}")
                throw TestBatchExecutionException(e)
            } catch(e: DeviceFailureException) {
                logger.error("$e")
                healthy = false
                throw DeviceLostException(e)
            } finally {
                logParser.close()
            }

            logger.info("Diagnostic logs available at ${logParser.diagnosticLogPaths}")
            // 70 = no devices
            // 65 = ** TEST EXECUTE FAILED **: crash
            logger.debug("Finished test batch execution with exit status $exitStatus")
        }
    }

    override suspend fun prepare(configuration: Configuration) {
        launch(deviceContext) {
            RemoteFileManager.createRemoteDirectory(this@IOSDevice)

            val sshjCommandExecutor = hostCommandExecutor as SshjCommandExecutor
            val derivedDataManager = DerivedDataManager(configuration)

            val remoteXctestrunFile = RemoteFileManager.remoteXctestrunFile(this@IOSDevice)
            val xctestrunFile = prepareXctestrunFile(derivedDataManager, remoteXctestrunFile)

            derivedDataManager.sendSynchronized(
                    localPath = xctestrunFile,
                    remotePath = remoteXctestrunFile.absolutePath,
                    hostName = sshjCommandExecutor.hostAddress.hostName,
                    port = sshjCommandExecutor.port
            )

            derivedDataManager.sendSynchronized(
                    localPath = derivedDataManager.productsDir,
                    remotePath = RemoteFileManager.remoteDirectory(this@IOSDevice).path,
                    hostName = sshjCommandExecutor.hostAddress.hostName,
                    port = sshjCommandExecutor.port
            )
        }
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
