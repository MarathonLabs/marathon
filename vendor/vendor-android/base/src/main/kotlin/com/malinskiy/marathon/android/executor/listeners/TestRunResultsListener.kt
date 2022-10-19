package com.malinskiy.marathon.android.executor.listeners

import com.malinskiy.marathon.android.model.AndroidTestResult
import com.malinskiy.marathon.android.model.AndroidTestStatus
import com.malinskiy.marathon.android.model.TestIdentifier
import com.malinskiy.marathon.android.model.TestRunResultsAccumulator
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.Attachment
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.attachment.AttachmentListener
import com.malinskiy.marathon.report.attachment.AttachmentProvider
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.test.toClassName
import com.malinskiy.marathon.test.toTestName
import com.malinskiy.marathon.time.Timer
import kotlinx.coroutines.CompletableDeferred

class TestRunResultsListener(
    private val testBatch: TestBatch,
    private val device: Device,
    private val deferred: CompletableDeferred<TestBatchResults>,
    private val timer: Timer,
    private val progressReporter: ProgressReporter,
    private val poolId: DevicePoolId,
    attachmentProviders: List<AttachmentProvider>
) : AbstractTestRunResultListener(timer), AttachmentListener {

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

    override suspend fun afterTestRun() {
        val results = mergeParameterisedResults(runResult.testResults)
        val tests = testBatch.tests.associateBy { it.identifier() }

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
            results[it.test.identifier()]?.isSuccessful() ?: false
        }

        val (reportedIncompleteTests, reportedNonNullTests) = nonNullTestResults.partition { it.status == TestStatus.INCOMPLETE }

        val failed = reportedNonNullTests.filterNot {
            val status = results[it.test.identifier()]
            when {
                status?.isSuccessful() == true -> true
                else -> false
            }
        }

        val uncompleted = reportedIncompleteTests + tests
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
        testRunResult: TestRunResultsAccumulator,
        device: Device
    ): Collection<TestResult> {

        val lastCompletedTestEndTime = testRunResult
            .testResults
            .values
            .maxByOrNull { it.endTime }
            ?.endTime
            ?: creationTime

        return map {
            TestResult(
                it,
                device.toDeviceInfo(),
                testBatch.id,
                TestStatus.INCOMPLETE,
                lastCompletedTestEndTime,
                timer.currentTimeMillis(),
                testRunResult.runFailureMessage
            )
        }
    }

    private fun mergeParameterisedResults(results: MutableMap<TestIdentifier, AndroidTestResult>): Map<TestIdentifier, AndroidTestResult> {
        /**
         * If we explicitly requested parameterized tests - skip merging
         */
        if (testBatch.tests.any { it.method.contains('[') && it.method.contains(']') }) return results

        val result = mutableMapOf<TestIdentifier, AndroidTestResult>()
        for (e in results) {
            val test = e.key
            if (test.testName.matches(""".+\[\d+]""".toRegex())) {
                val realIdentifier = TestIdentifier(e.key.className, e.key.testName.split("[")[0])
                val maybeExistingParameterizedResult = result[realIdentifier]
                if (maybeExistingParameterizedResult == null) {
                    result[realIdentifier] = e.value
                } else {
                    result[realIdentifier]?.status = maybeExistingParameterizedResult.status + e.value.status
                    //Needed for proper result aggregation
                    progressReporter.addTestDiscoveredDuringRuntime(poolId, test.toTest())
                }
            } else {
                result[test] = e.value
            }
        }

        return result.toMap()
    }

    private fun Map.Entry<TestIdentifier, AndroidTestResult>.toTestResult(device: Device): TestResult {
        val testInstanceFromBatch = testBatch.tests.find { it.toClassName() == key.className && it.method == key.testName }
        val test = key.toTest()
        val attachments = attachments[test] ?: emptyList()
        return TestResult(
            test = testInstanceFromBatch ?: test,
            device = device.toDeviceInfo(),
            testBatchId = testBatch.id,
            status = value.status.toMarathonStatus(),
            startTime = value.startTime,
            endTime = value.endTime,
            stacktrace = value.stackTrace,
            attachments = attachments
        )
    }

    private fun Test.identifier(): TestIdentifier {
        val classname = StringBuilder().apply {
            if (pkg.isNotEmpty()) {
                append("${pkg}.")
            }
            append(clazz)
        }.toString()

        return TestIdentifier(classname, method)
    }

    private fun AndroidTestResult.isSuccessful(): Boolean =
        when (status) {
            AndroidTestStatus.PASSED, AndroidTestStatus.IGNORED, AndroidTestStatus.ASSUMPTION_FAILURE -> true
            else -> false
        }
}

private operator fun AndroidTestStatus.plus(value: AndroidTestStatus): AndroidTestStatus {
    return when (this) {
        AndroidTestStatus.FAILURE -> AndroidTestStatus.FAILURE
        AndroidTestStatus.PASSED -> value
        AndroidTestStatus.IGNORED -> AndroidTestStatus.IGNORED
        AndroidTestStatus.INCOMPLETE -> AndroidTestStatus.INCOMPLETE
        AndroidTestStatus.ASSUMPTION_FAILURE -> AndroidTestStatus.ASSUMPTION_FAILURE
    }
}
