package com.malinskiy.marathon.android.adam

import com.malinskiy.adam.Const
import com.malinskiy.adam.request.testrunner.InstrumentOptions
import com.malinskiy.adam.request.testrunner.TestAssumptionFailed
import com.malinskiy.adam.request.testrunner.TestEnded
import com.malinskiy.adam.request.testrunner.TestEvent
import com.malinskiy.adam.request.testrunner.TestFailed
import com.malinskiy.adam.request.testrunner.TestIgnored
import com.malinskiy.adam.request.testrunner.TestRunEnded
import com.malinskiy.adam.request.testrunner.TestRunFailed
import com.malinskiy.adam.request.testrunner.TestRunStartedEvent
import com.malinskiy.adam.request.testrunner.TestRunStopped
import com.malinskiy.adam.request.testrunner.TestRunnerRequest
import com.malinskiy.adam.request.testrunner.TestStarted
import com.malinskiy.adam.request.transform.InstrumentationResponseTransformer
import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.android.ApkParser
import com.malinskiy.marathon.android.InstrumentationInfo
import com.malinskiy.marathon.android.executor.listeners.AndroidTestRunListener
import com.malinskiy.marathon.android.model.TestIdentifier
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.test.toTestName
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.receiveOrNull
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.util.concurrent.TimeoutException

const val JUNIT_IGNORE_META_PROPERTY_NAME = "org.junit.Ignore"
const val ERROR_STUCK = "Test got stuck. You can increase the timeout in settings if it's too strict"

class AndroidDeviceTestRunner(private val device: AdamAndroidDevice) {
    private val logger = MarathonLogging.logger("AndroidDeviceTestRunner")

    @OptIn(ExperimentalStdlibApi::class)
    suspend fun execute(
        configuration: Configuration,
        rawTestBatch: TestBatch,
        listener: AndroidTestRunListener
    ) {

        val ignoredTests = rawTestBatch.tests.filter { test ->
            test.metaProperties.any { it.name == JUNIT_IGNORE_META_PROPERTY_NAME }
        }
        val testBatch = TestBatch(rawTestBatch.tests - ignoredTests)

        val androidConfiguration = configuration.vendorConfiguration as AndroidConfiguration
        val info = ApkParser().parseInstrumentationInfo(androidConfiguration.testApplicationApk)
        val runnerRequest = prepareTestRunnerRequest(androidConfiguration, info, testBatch)

        try {
            notifyIgnoredTest(ignoredTests, listener)
            if (testBatch.tests.isNotEmpty()) {
                clearData(androidConfiguration, info)
                withTimeout(configuration.testBatchTimeoutMillis) {
                    val transformer = InstrumentationResponseTransformer()
                    val channel = device.executeTestRequest(runnerRequest)

                    var logPart: String? = null
                    do {
                        logPart?.let {
                            for (line in it.lines()) {
                                val bytes = (line + '\n').toByteArray(Const.DEFAULT_TRANSPORT_ENCODING)
                                transformer.process(bytes, 0, bytes.size)
                                transformer.transform()?.let { events ->
                                    processEvents(events, listener)
                                }
                            }
                        }
                        withTimeout(configuration.testOutputTimeoutMillis) {
                            logPart = channel.receiveOrNull()
                        }
                    } while (logPart != null)

                    transformer.close()?.let { events ->
                        processEvents(events, listener)
                    }
                }
            } else {
                listener.testRunEnded(0, emptyMap())
            }
        } catch (e: TimeoutCancellationException) {
            logger.warn(ERROR_STUCK)
            listener.testRunFailed(ERROR_STUCK)
        } catch (e: TimeoutException) {
            logger.warn(ERROR_STUCK)
            listener.testRunFailed(ERROR_STUCK)
        } catch (e: IOException) {
            val errorMessage = "adb error while running tests ${testBatch.tests.map { it.toTestName() }}"
            logger.error(e) { errorMessage }
            listener.testRunFailed(errorMessage)
        } finally {

        }
    }

    private suspend fun processEvents(
        events: List<TestEvent>,
        listener: AndroidTestRunListener
    ) {
        for (event in events) {
            when (event) {
                is TestRunStartedEvent -> listener.testRunStarted("AdamTestRun", event.testCount)
                is TestStarted -> listener.testStarted(event.id.toMarathonTestIdentifier())
                is TestFailed -> listener.testFailed(event.id.toMarathonTestIdentifier(), event.stackTrace)
                is TestAssumptionFailed -> listener.testAssumptionFailure(
                    event.id.toMarathonTestIdentifier(),
                    event.stackTrace
                )
                is TestIgnored -> listener.testIgnored(event.id.toMarathonTestIdentifier())
                is TestEnded -> listener.testEnded(event.id.toMarathonTestIdentifier(), event.metrics)
                is TestRunFailed -> listener.testRunFailed(event.error)
                is TestRunStopped -> listener.testRunStopped(event.elapsedTimeMillis)
                is TestRunEnded -> listener.testRunEnded(event.elapsedTimeMillis, event.metrics)
            }
        }
    }

    private suspend fun notifyIgnoredTest(ignoredTests: List<Test>, listeners: AndroidTestRunListener) {
        ignoredTests.forEach {
            val identifier = TestIdentifier("${it.pkg}.${it.clazz}", it.method)
            listeners.testStarted(identifier)
            listeners.testIgnored(identifier)
            listeners.testEnded(identifier, hashMapOf())
        }
    }

    private suspend fun clearData(androidConfiguration: AndroidConfiguration, info: InstrumentationInfo) {
        if (androidConfiguration.applicationPmClear) {
            device.safeClearPackage(info.applicationPackage)?.also {
                logger.debug { "Package ${info.applicationPackage} cleared: $it" }
            }
        }
        if (androidConfiguration.testApplicationPmClear) {
            device.safeClearPackage(info.instrumentationPackage)?.also {
                logger.debug { "Package ${info.instrumentationPackage} cleared: $it" }
            }
        }
        if (androidConfiguration.allureConfiguration.enabled) {
            device.fileManager.removeRemotePath(androidConfiguration.allureConfiguration.resultsDirectory, recursive = true)
            device.fileManager.createRemoteDirectory(androidConfiguration.allureConfiguration.resultsDirectory)
            if (device.version.isGreaterOrEqualThan(30)) {
                val command = "appops set --uid ${info.applicationPackage} MANAGE_EXTERNAL_STORAGE allow"
                device.safeExecuteShellCommand(command)?.also {
                    logger.debug { "Granted MANAGE_EXTERNAL_STORAGE to ${info.applicationPackage}: $it" }
                }
            }
        }
    }

    private fun prepareTestRunnerRequest(
        androidConfiguration: AndroidConfiguration,
        info: InstrumentationInfo,
        testBatch: TestBatch
    ): TestRunnerRequest {
        val tests = testBatch.tests.map {
            "${it.pkg}.${it.clazz}#${it.method}"
        }

        logger.debug { "tests = ${tests.toList()}" }

        return TestRunnerRequest(
            testPackage = info.instrumentationPackage,
            runnerClass = info.testRunnerClass,
            noWindowAnimations = true,
            instrumentOptions = InstrumentOptions(
                clazz = tests,
                overrides = androidConfiguration.instrumentationArgs
            )
        )
    }
}

private fun com.malinskiy.adam.request.testrunner.TestIdentifier.toMarathonTestIdentifier(): TestIdentifier {
    return TestIdentifier(this.className, this.testName)
}
