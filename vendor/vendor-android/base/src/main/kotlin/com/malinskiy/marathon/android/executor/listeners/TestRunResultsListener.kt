package com.malinskiy.marathon.android.executor.listeners

import com.malinskiy.marathon.android.model.TestIdentifier
import com.malinskiy.marathon.android.model.TestRunResult
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

class TestRunResultsListener(
    private val testBatch: TestBatch,
    private val device: Device,
    private val deferred: CompletableDeferred<TestBatchResults>,
    private val timer: Timer,
    attachmentProviders: List<AttachmentProvider>
) : AbstractTestRunResultListener(), AttachmentListener {

    private val attachments: MutableMap<Test, MutableList<Attachment>> = mutableMapOf()
    private val creationTime = timer.currentTimeMillis()

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

    override fun handleTestRunResults(runResult: TestRunResult) {
        val results = mergeParameterisedResults(runResult.getTestResults())
        val tests = testBatch.tests.associateBy { it.identifier() }

        val testResults = results.map {
            it.toTestResult(device)
        }

        val nonNullTestResults = testResults.filter {
            it.test.method != "null"
        }

        val finished = nonNullTestResults.filter {
            results[it.test]?.isSuccessful() ?: false
        }

        val failed = nonNullTestResults.filterNot {
            results[it.test]?.isSuccessful() ?: false
        }

        val uncompleted = tests
            .filterNot { expectedTest ->
                results.containsKey(expectedTest.value)
            }
            .values
            .createUncompletedTestResults(runResult, device)

        if (uncompleted.isNotEmpty()) {
            uncompleted.forEach {
                logger.warn { "uncompleted = ${it.test.toTestName()}, ${device.serialNumber}" }
            }
        }

        deferred.complete(TestBatchResults(device, finished, failed, uncompleted))
    }

    private fun Collection<Test>.createUncompletedTestResults(
        testRunResult: TestRunResult,
        device: Device
    ): Collection<TestResult> {

        val lastCompletedTestEndTime = testRunResult
            .getTestResults()
            .values
            .maxBy { it.endTime }
            ?.endTime
            ?: creationTime

        return map {
            TestResult(
                it,
                device.toDeviceInfo(),
                TestStatus.INCOMPLETE,
                lastCompletedTestEndTime,
                timer.currentTimeMillis(),
                testRunResult.getRunFailureMessage()
            )
        }
    }

    private fun mergeParameterisedResults(results: MutableMap<Test, TestResult>): Map<Test, TestResult> {
        val result = mutableMapOf<Test, TestResult>()
        for (e in results) {
            val test = e.key
            if (test.method.matches(""".+\[\d+]""".toRegex())) {
                val realIdentifier = Test(test.pkg, test.clazz, test.method.split("[")[0], test.metaProperties)
                val maybeExistingParameterizedResult = result[realIdentifier]
                if (maybeExistingParameterizedResult == null) {
                    result[realIdentifier] = e.value
                } else {
                    result[realIdentifier] = TestResult(
                        maybeExistingParameterizedResult.test,
                        maybeExistingParameterizedResult.device,
                        maybeExistingParameterizedResult.status + e.value.status,
                        maybeExistingParameterizedResult.startTime,
                        maybeExistingParameterizedResult.endTime,
                        maybeExistingParameterizedResult.stacktrace,
                        maybeExistingParameterizedResult.attachments
                    )
                }
            } else {
                result[test] = e.value
            }
        }

        return result.toMap()
    }

    private fun Map.Entry<Test, TestResult>.toTestResult(device: Device): TestResult {
        val testInstanceFromBatch = testBatch.tests.find { "${it.pkg}.${it.clazz}" == key.clazz && it.method == key.method }
        val test = key
        val attachments = attachments[test] ?: emptyList<Attachment>()
        return TestResult(
            test = testInstanceFromBatch ?: test,
            device = device.toDeviceInfo(),
            status = value.status,
            startTime = value.startTime,
            endTime = value.endTime,
            stacktrace = value.stacktrace,
            attachments = attachments
        )
    }

    private fun Test.identifier(): TestIdentifier {
        return TestIdentifier("$pkg.$clazz", method)
    }

    private fun TestResult.isSuccessful() =
        status == TestStatus.PASSED ||
                status == TestStatus.IGNORED ||
                status == TestStatus.ASSUMPTION_FAILURE

}

private operator fun TestStatus.plus(value: TestStatus): TestStatus {
    return when (this) {
        TestStatus.FAILURE -> TestStatus.FAILURE
        TestStatus.PASSED -> value
        TestStatus.IGNORED -> TestStatus.IGNORED
        TestStatus.INCOMPLETE -> TestStatus.INCOMPLETE
        TestStatus.ASSUMPTION_FAILURE -> TestStatus.ASSUMPTION_FAILURE
    }
}
