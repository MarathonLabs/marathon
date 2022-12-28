package com.malinskiy.marathon.ios.executor.listener

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.execution.result.TemporalTestResult
import com.malinskiy.marathon.ios.RemoteFileManager
import com.malinskiy.marathon.ios.bin.xcrun.xcresulttool.ResultBundleFormat
import com.malinskiy.marathon.ios.bin.xcrun.xcresulttool.Xcresulttool
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.attachment.AttachmentCollector
import com.malinskiy.marathon.report.attachment.AttachmentListener
import com.malinskiy.marathon.report.attachment.AttachmentProvider
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.test.toTestName
import com.malinskiy.marathon.time.Timer
import com.malinskiy.marathon.vendor.ios.xcrun.xcresulttool.ActionTestPlanRunSummaries
import com.malinskiy.marathon.vendor.ios.xcrun.xcresulttool.ActionsInvocationRecord
import kotlinx.coroutines.CompletableDeferred
import kotlin.system.measureTimeMillis

class TestResultsListener(
    private val testBatch: TestBatch,
    private val device: Device,
    private val deferred: CompletableDeferred<TestBatchResults>,
    private val timer: Timer,
    private val remoteFileManager: RemoteFileManager,
    private val xcresulttool: Xcresulttool,
    attachmentProviders: List<AttachmentProvider>,
    private val attachmentCollector: AttachmentCollector = AttachmentCollector(attachmentProviders),
) : AccumulatingTestResultListener(testBatch.tests.size, timer), AttachmentListener by attachmentCollector {

    private val logger = MarathonLogging.logger {}
    private val enhanceUsingXcresult = true

    override suspend fun afterTestRun() {
        if(enhanceUsingXcresult) enhanceResults()

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

    private suspend fun enhanceResults() {
        measureTimeMillis {
            val remoteXcresult = remoteFileManager.remoteXcresultFile(testBatch)
            val actionsInvocationRecord =
                xcresulttool.get(ActionsInvocationRecord::class.java, remoteXcresult, ResultBundleFormat.JSON) ?: return

            val targetName = actionsInvocationRecord.actions.mapNotNull {
                it.actionResult.testsRef?.id?.let {
                    xcresulttool.get(ActionTestPlanRunSummaries::class.java, remoteXcresult, ResultBundleFormat.JSON, id = it)?.summaries
                }
            }.flatten().mapNotNull { it.testableSummaries.mapNotNull { it.targetName } }.flatten().distinct()

            val pkg = when (targetName.size) {
                1 -> targetName.first()
                0 -> {
                    logger.warn { "No testable targets found in xcresult" }
                    null
                }

                else -> {
                    logger.warn { "Multiple testing targets found in xcresult" }
                    null
                }
            }

            actionsInvocationRecord.issues.testFailureSummaries.forEach { failureSummary ->
                //AIR doesn't contain package information at all. Match by class+method and fail if more than one found
                val matchingTests = runResult.completedTests.filter {
                    val testCaseName = failureSummary.testCaseName.removeSuffix("()")
                    if (pkg != null) {
                        it.pkg == pkg && "${it.clazz}.${it.method}" == testCaseName
                    } else {
                        "${it.clazz}.${it.method}" == testCaseName
                    }
                }
                when (matchingTests.size) {
                    1 -> runResult.testFailed(
                        matchingTests.first(),
                        trace = StringBuilder().apply {
                            failureSummary.documentLocationInCreatingWorkspace?.let { appendLine(it.url) }
                            appendLine(failureSummary.message)
                        }.toString()
                    )

                    0 -> logger.warn { "No matching test cases found in xcresult" }
                    else -> logger.warn { "Multiple matching test cases found in xcresult. Impossible to possible to add stacktrace to report" }
                }
            }
        }.let {
            logger.debug { "Enhancing report using xcresult took $it ms" }
        }
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
