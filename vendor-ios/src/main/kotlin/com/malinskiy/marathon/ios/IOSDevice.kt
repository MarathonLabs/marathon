package com.malinskiy.marathon.ios


import com.google.gson.Gson
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.device.OperatingSystem
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.ios.cmd.remote.CommandExecutor
import com.malinskiy.marathon.ios.cmd.remote.SshjCommandExecutor
import com.malinskiy.marathon.ios.device.RemoteSimulatorFeatureProvider
import com.malinskiy.marathon.ios.logparser.CompositeLogParser
import com.malinskiy.marathon.ios.logparser.DebugLoggingParser
import com.malinskiy.marathon.ios.logparser.TestRunProgressParser
import com.malinskiy.marathon.ios.logparser.formatter.TestLogPackageNameFormatter
import com.malinskiy.marathon.ios.logparser.listener.ProgressReportingListener
import com.malinskiy.marathon.ios.logparser.listener.TestLogListener
import com.malinskiy.marathon.ios.simctl.Simctl
import com.malinskiy.marathon.ios.xctestrun.Xctestrun
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.time.SystemTimer
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import net.schmizz.sshj.connection.ConnectionException
import net.schmizz.sshj.transport.TransportException
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

private const val HOSTNAME = "localhost"

class IOSDevice(val udid: String,
                val hostCommandExecutor: CommandExecutor,
                val gson: Gson) : Device {
    val logger = MarathonLogging.logger("${javaClass.simpleName}($udid)")
    val simctl = Simctl()

    val name: String?
    private val runtime: String?
    private val deviceType: String?

    private val deviceContext = newFixedThreadPoolContext(1, udid)

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
    override val healthy: Boolean
        get() = true
    override val abi: String
        get() = "Simulator"

    override suspend fun execute(configuration: Configuration,
                                 devicePoolId: DevicePoolId,
                                 testBatch: TestBatch,
                                 deferred: CompletableDeferred<TestBatchResults>,
                                 progressReporter: ProgressReporter) {

        launch(deviceContext) {
            val iosConfiguration = configuration.vendorConfiguration as IOSConfiguration
            val fileManager = FileManager(configuration.outputDir)
            val testLogListener = TestLogListener()

            val remoteXcresultPath = RemoteFileManager.remoteXcresultFile(this@IOSDevice)
            val remoteXctestrunFile = RemoteFileManager.remoteXctestrunFile(this@IOSDevice)
            val remoteDir = remoteXctestrunFile.parent

            logger.debug { "remote xctestrun = $remoteXctestrunFile" }

            val xctestrun = Xctestrun(iosConfiguration.xctestrunPath)
            val packageNameFormatter = TestLogPackageNameFormatter(xctestrun.productModuleName, xctestrun.targetName)

            val logParser = CompositeLogParser(listOf(
                    //Order matters here: first grab the log with log listener,
                    //then use this log to insert into the test report
                    testLogListener,
                    TestRunProgressParser(SystemTimer(),
                            packageNameFormatter,
                            listOf(
                                    ProgressReportingListener(
                                            this@IOSDevice,
                                            devicePoolId,
                                            progressReporter,
                                            deferred,
                                            testBatch,
                                            testLogListener
                                    ),
                                    testLogListener
                            )
                    ),
                    DebugLoggingParser()
            ))

            val tests = testBatch.tests.map {
                "${it.pkg}.${it.clazz}#${it.method}"
            }.toTypedArray()

            logger.debug { "tests = ${tests.toList()}" }

            val testBatchToArguments = testBatch.tests
                    .map { "-only-testing:\"${it.pkg}/${it.clazz}/${it.method}\"" }
                    .joinToString(separator = " ")

            val remoteCommand =
                    listOf("cd '$remoteDir' &&",
                            "NSUnbufferedIO=YES",
                            "xcodebuild test-without-building",
                            "-xctestrun ${remoteXctestrunFile.path}",
                            // "-resultBundlePath ${remoteXcresultPath.canonicalPath} ",
                            testBatchToArguments,
                            "-destination 'platform=iOS simulator,id=$udid' ;",
                            "exit")
                            .joinToString(" ")
                            .also { logger.debug(it) }
            val session = hostCommandExecutor.startSession()
            try {
                val command = session.exec(remoteCommand)

                command.inputStream.reader().forEachLine {  logParser.onLine(it)  }
                command.errorStream.reader().forEachLine {  logger.error(it) }

                command.join(configuration.testOutputTimeoutMillis, TimeUnit.MILLISECONDS)
            } catch(e: ConnectionException) {
                logger.error("Ssh exception: ${e}")
            } catch(e: TransportException) {
                logger.error("Ssh exception: ${e}")
            } finally {
                logParser.close()

                if (session.isOpen) {
                    try {
                        session.close()
                    } catch (e: IOException) { }
                }
            }
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
}
