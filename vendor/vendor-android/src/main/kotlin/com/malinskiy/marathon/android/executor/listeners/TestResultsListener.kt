package com.malinskiy.marathon.android.executor.listeners

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.execution.progress.ProgressReporter
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
    private val poolId: DevicePoolId,
    attachmentProviders: List<AttachmentProvider>,
    private val attachmentCollector: AttachmentCollector = AttachmentCollector(attachmentProviders),
) : AccumulatingResultTestRunListener(timer), AttachmentListener by attachmentCollector {

    private val logger = MarathonLogging.logger("TestRunResultsListener")

    override suspend fun afterTestRun() {
        val results = mergeParameterisedResults(runResult.temporalTestResults)
        val tests = testBatch.tests

        val testResults = results.map {
            it.toTestResult(device)
        }

        val nonNullTestResults = testResults.filter {
            /**
             * If we have a result with null method, then we ignore it unless explicitly requested to
             *
             * An example of null method response is @BeforeClass failure
             * An example of null method request is an ignored test class with remote parser
             */
            it.test.method != "null" || testBatch.tests.contains(it.test)
        }

        val finished = nonNullTestResults.filter {
            results[it.test]?.isSuccessful() ?: false
        }

        val (reportedIncompleteTests, reportedNonNullTests) = nonNullTestResults.partition { it.status == TestStatus.INCOMPLETE }

        val failed = reportedNonNullTests.filterNot {
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

    private fun mergeParameterisedResults(results: LinkedHashMap<Test, TemporalTestResult>): Map<Test, TemporalTestResult> {
        /**
         * If we explicitly requested parameterized tests - skip merging
         */
        if (testBatch.tests.any { it.method.contains('[') && it.method.contains(']') }) return results

        val result = mutableMapOf<Test, TemporalTestResult>()
        for (e in results) {
            val test = e.key
            if (test.method.matches(""".+\[\d+]""".toRegex())) {
                val realIdentifier = Test(e.key.pkg, e.key.clazz, e.key.method.split("[")[0], emptyList())
                val maybeExistingParameterizedResult = result[realIdentifier]
                if (maybeExistingParameterizedResult == null) {
                    result[realIdentifier] = e.value
                } else {
                    result[realIdentifier]?.status = maybeExistingParameterizedResult.status + e.value.status
                }
            } else {
                result[test] = e.value
            }
        }

        return result.toMap()
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
