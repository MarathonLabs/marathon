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
import com.malinskiy.marathon.report.html.relativePathTo
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.time.SystemTimer
import kotlinx.coroutines.experimental.CompletableDeferred
import java.io.FileNotFoundException

private const val HOSTNAME = "localhost"

class IOSDevice(val udid: String,
                val hostCommandExecutor: CommandExecutor,
                val gson: Gson) : Device {

    val logger = MarathonLogging.logger("${javaClass.simpleName}($udid)")
    val simctl = Simctl()
    val runtime: String?
    val name: String?
    val deviceType: String?

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
    override val deviceFeatures: Collection<DeviceFeature>
        get() = RemoteSimulatorFeatureProvider.deviceFeatures(this)
    override val healthy: Boolean
        get() = true
    override val abi: String
        get() = "Simulator"

    override fun execute(configuration: Configuration,
                         devicePoolId: DevicePoolId,
                         testBatch: TestBatch,
                         deferred: CompletableDeferred<TestBatchResults>,
                         progressReporter: ProgressReporter) {

        val iosConfiguration = configuration.vendorConfiguration as IOSConfiguration
        val fileManager = FileManager(configuration.outputDir)
        val testLogListener = TestLogListener()

        val remoteXcresultPath = RemoteFileManager.remoteXcresultFile(this)
        val remoteXctestrunFile = RemoteFileManager.remoteXctestrunFile(this)
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
                                        this,
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

        val session = hostCommandExecutor.startSession()
        val exec = listOf(
                "cd '$remoteDir' &&",
                "NSUnbufferedIO=YES",
                "xcodebuild test-without-building",
                "-xctestrun ${remoteXctestrunFile.path}",
                // "-resultBundlePath ${remoteXcresultPath.canonicalPath} ",
                testBatchToArguments,
                "-destination 'platform=iOS simulator,id=$udid'").joinToString(" ").also { logger.debug(it) }
        val command = session.exec(exec)

        try {
            command.inputStream.reader().forEachLine { logParser.onLine(it) }
        } catch (e: Exception) {
            logger.error { "Exception $e" }
        }

        command.errorStream.reader().forEachLine { logger.error(it) }

        logParser.close()

        session.close()
    }

    override fun prepare(configuration: Configuration) {
        val sshjCommandExecutor = hostCommandExecutor as SshjCommandExecutor

        val derivedDataManager = DerivedDataManager(configuration)
        RemoteFileManager.createRemoteDirectory(this)

        // 1. remote paths
        val remoteProductsDir = RemoteFileManager.remoteDirectory(this)
        val remoteXctestrunFile = RemoteFileManager.remoteXctestrunFile(this)

        // 2. local paths
        val productsDir = derivedDataManager.productsDir
        val xctestrunPath = (configuration.vendorConfiguration as IOSConfiguration).xctestrunPath
        if (xctestrunPath.relativePathTo(productsDir) != xctestrunPath.name) {
            throw FileNotFoundException("xctestrun file must be located in build products directory.")
        }
        val xctestrunFile = productsDir.resolve(remoteXctestrunFile.name)

        // 3. a port
        val remotePort = RemoteSimulatorFeatureProvider.availablePort(this)
                .also { logger.debug("Using TCP port $it on device $udid") }

        // 4. Update xctestrun environment
        val xctestrun = Xctestrun(xctestrunPath)
        xctestrun.environment("TEST_HTTP_SERVER_PORT", "$remotePort")
        logger.debug("Updating xctestrun environment with TEST_HTTP_SERVER_PORT=$remotePort")

        // 5. Save under the new name
        xctestrunFile.writeBytes(xctestrun.toXMLByteArray())

        // send the prepared xctestrun
        logger.debug("Sending xctestrun file from $xctestrunFile to $remoteXctestrunFile")
        derivedDataManager.sendSynchronized(
                localPath = xctestrunFile,
                remotePath = remoteXctestrunFile.absolutePath,
                hostName = sshjCommandExecutor.hostAddress.hostName,
                port = sshjCommandExecutor.port
        )

        // copy build products
        logger.debug("Sending build products from $productsDir to $remoteProductsDir")
        derivedDataManager.send(
                localPath = productsDir,
                remotePath = remoteProductsDir.path,
                hostName = sshjCommandExecutor.hostAddress.hostName,
                port = sshjCommandExecutor.port
        )
    }
}
