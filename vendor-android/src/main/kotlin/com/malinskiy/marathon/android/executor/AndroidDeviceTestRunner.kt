package com.malinskiy.marathon.android.executor

import com.android.ddmlib.AdbCommandRejectedException
import com.android.ddmlib.ShellCommandUnresponsiveException
import com.android.ddmlib.TimeoutException
import com.android.ddmlib.testrunner.RemoteAndroidTestRunner
import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.ApkParser
import com.malinskiy.marathon.android.executor.listeners.CompositeTestRunListener
import com.malinskiy.marathon.android.executor.listeners.DebugTestRunListener
import com.malinskiy.marathon.android.executor.listeners.LogCatListener
import com.malinskiy.marathon.android.executor.listeners.ProgressTestRunListener
import com.malinskiy.marathon.android.executor.listeners.video.ScreenRecorderTestRunListener
import com.malinskiy.marathon.android.executor.listeners.TestRunResultsListener
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.logs.LogWriter
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.test.toTestName
import kotlinx.coroutines.experimental.CompletableDeferred
import java.io.IOException
import java.util.concurrent.TimeUnit

class AndroidDeviceTestRunner(private val device: AndroidDevice) {

    private val logger = MarathonLogging.logger("AndroidDeviceTestRunner")

    fun execute(configuration: Configuration,
                devicePoolId: DevicePoolId,
                testBatch: TestBatch,
                deferred: CompletableDeferred<TestBatchResults>,
                progressReporter: ProgressReporter) {

        val androidConfiguration = configuration.vendorConfiguration as AndroidConfiguration
        val info = ApkParser().parseInstrumentationInfo(androidConfiguration.testApplicationOutput)
        val runner = RemoteAndroidTestRunner(info.instrumentationPackage, info.testRunnerClass, device.ddmsDevice)
        runner.setRunName("TestRunName")
        runner.setMaxTimeToOutputResponse(configuration.testOutputTimeoutMillis.toLong() * testBatch.tests.size, TimeUnit.MILLISECONDS)

        val tests = testBatch.tests.map {
            "${it.pkg}.${it.clazz}#${it.method}"
        }.toTypedArray()

        logger.debug { "tests = ${tests.toList()}" }

        runner.setClassNames(tests)
        val fileManager = FileManager(configuration.outputDir)
        val listeners = CompositeTestRunListener(listOf(
                TestRunResultsListener(testBatch, device, deferred),
                ScreenRecorderTestRunListener(fileManager, devicePoolId, device),
                DebugTestRunListener(device.ddmsDevice),
                ProgressTestRunListener(device, devicePoolId, progressReporter),
                LogCatListener(device, devicePoolId, LogWriter(fileManager)))
        )
        try {
            runner.run(listeners)
        } catch (e: ShellCommandUnresponsiveException) {
            logger.warn("Test got stuck. You can increase the timeout in settings if it's too strict")
        } catch (e: TimeoutException) {
            logger.warn("Test got stuck. You can increase the timeout in settings if it's too strict")
        } catch (e: AdbCommandRejectedException) {
            throw RuntimeException("Error while running tests ${testBatch.tests.map { it.toTestName() }}", e)
        } catch (e: IOException) {
            throw RuntimeException("Error while running tests ${testBatch.tests.map { it.toTestName() }}", e)
        } finally {

        }
    }
}
