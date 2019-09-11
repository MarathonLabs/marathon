package com.malinskiy.marathon.android.executor.listeners

import com.android.ddmlib.testrunner.TestIdentifier
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
import com.android.ddmlib.testrunner.TestRunResult as DdmLibTestRunResult

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

    override fun handleTestRunResults(runResult: DdmLibTestRunResult) {
        val results = mergeParameterisedResults(runResult.testResults)
        val tests = testBatch.tests.associateBy { it.identifier() }

        val testResults = results.map {
            it.toTestResult(device)
        }

        val nonNullTestResults = testResults.filter {
            it.test.method != "null"
        }

        val finished = nonNullTestResults.filter {
            results[it.test.identifier()]?.isSuccessful() ?: false
        }

        val failed = nonNullTestResults.filterNot {
            results[it.test.identifier()]?.isSuccessful() ?: false
        }

        val uncompleted = tests
            .filterNot { expectedTest ->
                results.containsKey(expectedTest.key)
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
        testRunResult: com.android.ddmlib.testrunner.TestRunResult,
        device: Device
    ): Collection<TestResult> {

        val lastCompletedTestEndTime = testRunResult
            .testResults
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


    private fun Test.identifier(): TestIdentifier {
        return TestIdentifier("$pkg.$clazz", method)
    }

    private fun DdmLibTestResult.isSuccessful() =
        status == DdmLibTestResult.TestStatus.PASSED ||
                status == DdmLibTestResult.TestStatus.IGNORED ||
                status == DdmLibTestResult.TestStatus.ASSUMPTION_FAILURE

}

private operator fun com.android.ddmlib.testrunner.TestResult.TestStatus.plus(value: com.android.ddmlib.testrunner.TestResult.TestStatus): com.android.ddmlib.testrunner.TestResult.TestStatus? {
    return when (this) {
        com.android.ddmlib.testrunner.TestResult.TestStatus.FAILURE -> com.android.ddmlib.testrunner.TestResult.TestStatus.FAILURE
        com.android.ddmlib.testrunner.TestResult.TestStatus.PASSED -> value
        com.android.ddmlib.testrunner.TestResult.TestStatus.INCOMPLETE -> com.android.ddmlib.testrunner.TestResult.TestStatus.INCOMPLETE
        com.android.ddmlib.testrunner.TestResult.TestStatus.ASSUMPTION_FAILURE -> com.android.ddmlib.testrunner.TestResult.TestStatus.ASSUMPTION_FAILURE
        com.android.ddmlib.testrunner.TestResult.TestStatus.IGNORED -> com.android.ddmlib.testrunner.TestResult.TestStatus.IGNORED
    }
}
