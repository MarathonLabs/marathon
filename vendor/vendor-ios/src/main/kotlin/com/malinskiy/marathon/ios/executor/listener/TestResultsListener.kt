package com.malinskiy.marathon.ios.executor.listener

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.execution.result.TemporalTestResult
import com.malinskiy.marathon.report.attachment.AttachmentCollector
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.attachment.AttachmentListener
import com.malinskiy.marathon.report.attachment.AttachmentProvider
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.test.toTestName
import com.malinskiy.marathon.time.Timer
import kotlinx.coroutines.CompletableDeferred

class TestResultsListener(
    private val testBatch: TestBatch,
    private val device: Device,
    private val deferred: CompletableDeferred<TestBatchResults>,
    private val timer: Timer,
    attachmentProviders: List<AttachmentProvider>,
    private val attachmentCollector: AttachmentCollector = AttachmentCollector(attachmentProviders),
) : AccumulatingTestResultListener(testBatch.tests.size, timer), AttachmentListener by attachmentCollector {

    private val logger = MarathonLogging.logger {}

    override suspend fun afterTestRun() {
        val results = runResult.temporalTestResults
        val tests = testBatch.tests

        val testResults = results.map {
            it.toTestResult(device)
        }

        val finished = testResults.filter {
            results[it.test]?.isSuccessful() ?: false
        }

        val (reportedIncompleteTests, reportedTests) = testResults.partition { it.status == TestStatus.INCOMPLETE }

        val failed = reportedTests.filterNot {
            val status = results[it.test]
            when {
                status?.isSuccessful() == true -> true
                else -> false
            }
        }

        val missingTests = tests.filterNot { expectedTest ->
            results.containsKey(expectedTest)
        }
        val calculatedIncompleteTests = runResult.createUncompletedTestResults(
            missingTests,
            testBatch,
            device.toDeviceInfo(),
        )
        val uncompleted = reportedIncompleteTests + calculatedIncompleteTests

        if (uncompleted.isNotEmpty()) {
            uncompleted.forEach {
                logger.warn { "uncompleted = ${it.test.toTestName()}, ${device.serialNumber}" }
            }
        }

        deferred.complete(TestBatchResults(device, finished, failed, uncompleted))
    }

    private fun Map.Entry<Test, TemporalTestResult>.toTestResult(device: Device): TestResult {
        val testInstanceFromBatch = testBatch.tests.find {
            it.pkg == key.pkg && it.clazz == key.pkg && it.method == key.method
        }
        val test = key
        val attachments = attachmentCollector[test] ?: emptyList()
        return TestResult(
            test = testInstanceFromBatch ?: test,
            device = device.toDeviceInfo(),
            testBatchId = testBatch.id,
            status = value.status,
            startTime = value.startTime,
            endTime = value.endTime,
            stacktrace = value.stackTrace,
            attachments = attachments
        )
    }
}
