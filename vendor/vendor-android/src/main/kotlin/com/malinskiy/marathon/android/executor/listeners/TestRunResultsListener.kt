package com.malinskiy.marathon.android.executor.listeners

import com.android.ddmlib.TimeoutException
import com.android.ddmlib.testrunner.TestIdentifier
import com.malinskiy.marathon.android.exception.deviceLostRegex
import com.malinskiy.marathon.android.toMarathonStatus
import com.malinskiy.marathon.android.toTest
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.Attachment
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.attachment.AttachmentListener
import com.malinskiy.marathon.report.attachment.AttachmentProvider
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.test.toTestName
import com.malinskiy.marathon.time.Timer
import kotlinx.coroutines.CompletableDeferred
import com.android.ddmlib.testrunner.TestResult as DdmLibTestResult
import com.android.ddmlib.testrunner.TestResult.TestStatus as DdmLibTestStatus
import com.android.ddmlib.testrunner.TestRunResult as DdmLibTestRunResult

class TestRunResultsListener(
    private val testBatch: TestBatch,
    private val device: Device,
    private val deferred: CompletableDeferred<TestBatchResults>,
    private val timer: Timer,
    attachmentProviders: List<AttachmentProvider>
) : AbstractTestRunResultListener(), AttachmentListener {

    private val attachments: MutableMap<Test, MutableList<Attachment>> = mutableMapOf()

    init {
        attachmentProviders.forEach {
            it.registerListener(this)
        }
    }

    override fun onAttachment(test: Test, attachment: Attachment) {
        val list = attachments[test]
        if (list == null) {
            attachments[test] = mutableListOf()
        }

        attachments[test]!!.add(attachment)
    }

    private val logger = MarathonLogging.logger("TestRunResultsListener")

    override fun handleTestRunResults(runResult: DdmLibTestRunResult) {
        val results = mergeParameterisedResults(runResult.testResults)
        val tests = testBatch.tests.associateBy { it.identifier() }

        val passed = mutableListOf<TestResult>()
        val failed = mutableListOf<TestResult>()
        val infraFailures = mutableListOf<TestResult>()
        val incomplete = mutableListOf<TestResult>()

        results.forEach { entry ->
            val result = entry.toTestResult(device)
            if (result.test.method == "null") return@forEach

            when (entry.value.status) {
                DdmLibTestStatus.PASSED, DdmLibTestStatus.ASSUMPTION_FAILURE, DdmLibTestStatus.IGNORED -> passed.add(result)

                DdmLibTestStatus.FAILURE -> {
                    val isTimedOut = result.stacktrace?.contains(TimeoutException::class.java.canonicalName) ?: false
                    val isDeviceLost = result.stacktrace?.contains(deviceLostRegex) ?: false

                    if (isTimedOut || isDeviceLost) {
                        logger.warn { "infraFailure = ${result.test.toTestName()}, ${device.serialNumber}" }
                        infraFailures.add(result)
                    } else {
                        failed.add(result)
                    }
                }

                DdmLibTestStatus.INCOMPLETE, null -> {
                    logger.warn { "uncompleted = ${result.test.toTestName()}, ${device.serialNumber}" }
                    incomplete.add(result)
                }
            }
        }

        val noStatus = tests
            .filterNot { expectedTest ->
                results.containsKey(expectedTest.key)
            }
            .values
            .createUncompletedTestResults(runResult, device)

        noStatus.forEach {
            logger.warn { "noStatus = ${it.test.toTestName()}, ${device.serialNumber}" }
        }

        logger.debug(
            "Batch test results: " +
                    "passed=${passed.size}; " +
                    "failed=${failed.size}; " +
                    "infraFailures=${infraFailures.size}; " +
                    "uncompleted=${incomplete.size}; " +
                    "noStatus=${noStatus.size}"
        )

        deferred.complete(
            TestBatchResults(
                device, passed, failed,
                uncompleted = infraFailures + incomplete + noStatus
            )
        )
    }

    private fun Collection<Test>.createUncompletedTestResults(
        testRunResult: com.android.ddmlib.testrunner.TestRunResult,
        device: Device
    ): Collection<TestResult> {

        val lastCompletedTestEndTime = testRunResult
            .testResults
            .values
            .maxBy { it.endTime }
            ?.endTime
            ?: timer.currentTimeMillis()

        return map {
            TestResult(
                it,
                device.toDeviceInfo(),
                TestStatus.INCOMPLETE,
                lastCompletedTestEndTime,
                lastCompletedTestEndTime,
                testRunResult.runFailureMessage
            )
        }
    }

    private fun mergeParameterisedResults(results: MutableMap<TestIdentifier, com.android.ddmlib.testrunner.TestResult>): Map<TestIdentifier, com.android.ddmlib.testrunner.TestResult> {
        val result = mutableMapOf<TestIdentifier, com.android.ddmlib.testrunner.TestResult>()
        for (e in results) {
            if (e.key.testName.matches(""".+\[\d+]""".toRegex())) {
                val realIdentifier = TestIdentifier(e.key.className, e.key.testName.split("[")[0])
                val maybeExistingParameterizedResult = result[realIdentifier]
                if (maybeExistingParameterizedResult == null) {
                    result[realIdentifier] = e.value
                } else {
                    maybeExistingParameterizedResult.status = maybeExistingParameterizedResult.status + e.value.status
                }
            } else {
                result[e.key] = e.value
            }
        }

        return result.toMap()
    }

    private fun Map.Entry<TestIdentifier, DdmLibTestResult>.toTestResult(device: Device): TestResult {
        val testInstanceFromBatch = testBatch.tests.find { "${it.pkg}.${it.clazz}" == key.className && it.method == key.testName }
        val test = key.toTest()
        val attachments = attachments[test] ?: emptyList<Attachment>()
        return TestResult(
            test = testInstanceFromBatch ?: test,
            device = device.toDeviceInfo(),
            status = value.status.toMarathonStatus(),
            startTime = value.startTime,
            endTime = value.endTime,
            stacktrace = value.stackTrace,
            attachments = attachments
        )
    }

    private fun Test.identifier() = TestIdentifier("$pkg.$clazz", method)
}

private operator fun DdmLibTestStatus.plus(value: DdmLibTestStatus) =
    when (this) {
        DdmLibTestStatus.FAILURE -> DdmLibTestStatus.FAILURE
        DdmLibTestStatus.PASSED -> value
        DdmLibTestStatus.INCOMPLETE -> DdmLibTestStatus.INCOMPLETE
        DdmLibTestStatus.ASSUMPTION_FAILURE -> DdmLibTestStatus.ASSUMPTION_FAILURE
        DdmLibTestStatus.IGNORED -> DdmLibTestStatus.IGNORED
    }
