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
import com.malinskiy.marathon.ios.simctl.Simctl
import com.malinskiy.marathon.ios.xcrun.CompositeLogParser
import com.malinskiy.marathon.ios.xcrun.DebugLoggingParser
import com.malinskiy.marathon.ios.xcrun.TestRunProgressParser
import com.malinskiy.marathon.ios.xcrun.listener.ProgressReportingListener
import com.malinskiy.marathon.ios.xcrun.listener.TestLogListener
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.time.SystemTimer
import kotlinx.coroutines.experimental.CompletableDeferred

private const val REMOTE_DIR = "/tmp/marathon"

class IOSDevice(val udid: String,
                val hostCommandExecutor: CommandExecutor,
                val gson: Gson) : Device {

    val logger = MarathonLogging.logger(javaClass.simpleName)
    val simctl = Simctl()
    val runtime: String?
    val name: String?

    init {
        val device = simctl.list(this, gson).find { it.udid == udid }
        runtime = device?.runtime
        name = device?.name
    }

    override val operatingSystem: OperatingSystem
        get() = OperatingSystem(runtime ?: "Unknown")
    override val serialNumber: String
        get() = udid
    override val model: String
        get() = name ?: "Unknown"
    override val manufacturer: String
        get() = "Apple"
    override val networkState: NetworkState
        get() = NetworkState.CONNECTED
    override val deviceFeatures: Collection<DeviceFeature>
        get() {
            val session = hostCommandExecutor.startSession()
            val command = session.exec(
                    "/usr/sbin/system_profiler -detailLevel mini -xml SPDisplaysDataType"
            )
            command.join()

            val result =
            if (command.inputStream.bufferedReader().readLines().any { it.contains("spdisplays_metalfeatureset") }) {
                logger.debug("${udid} has DeviceFeature.VIDEO")
                listOf(DeviceFeature.VIDEO, DeviceFeature.SCREENSHOT)
            } else
                listOf(DeviceFeature.SCREENSHOT)

            session.close()
            return result
        }
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

        val logParser = CompositeLogParser(listOf(
                //Order matters here: first grab the log with log listener,
                //then use this log to insert into the test report
                testLogListener,
                TestRunProgressParser(SystemTimer(),
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
                )),
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
        val command = session.exec("export NSUnbufferedIO=YES && " +
                "cd $REMOTE_DIR/$udid && " +
                "xcodebuild test-without-building " +
                "-xctestrun ${iosConfiguration.xctestrunPath.absolutePath} " +
                "$testBatchToArguments " +
                "-destination 'platform=iOS simulator,id=$udid'")

        command.join()

        command.inputStream.reader().forEachLine {
            logParser.onLine(it)
        }

        command.errorStream.bufferedReader().forEachLine { logger.error(it) }

        logParser.close()

        session.close()
    }

    override fun prepare(configuration: Configuration) {
        val iosConfiguration = configuration.vendorConfiguration as IOSConfiguration

        val productsDir = iosConfiguration
                .derivedDataDir
                .toPath()
                .resolve("Build/Products/")
                .toFile()
        val remoteDir = "$REMOTE_DIR/$udid/"

        val derivedDataManager = DerivedDataManager(configuration,"localhost", 22)

        logger.debug("Will copy from $productsDir to remote $remoteDir")
        derivedDataManager.send(productsDir, remoteDir)
    }
}
