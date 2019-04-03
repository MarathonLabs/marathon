package com.malinskiy.marathon.android.executor.listeners

import com.android.ddmlib.testrunner.TestIdentifier
import com.malinskiy.marathon.android.toMarathonStatus
import com.malinskiy.marathon.android.toTest
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.Attachment
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.attachment.AttachmentListener
import com.malinskiy.marathon.report.attachment.AttachmentProvider
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.test.toTestName
import kotlinx.coroutines.CompletableDeferred
import com.android.ddmlib.testrunner.TestResult as DdmLibTestResult
import com.android.ddmlib.testrunner.TestRunResult as DdmLibTestRunResult

class TestRunResultsListener(private val testBatch: TestBatch,
                             private val device: Device,
                             private val deferred: CompletableDeferred<TestBatchResults>,
                             private val attachmentProviders: List<AttachmentProvider>)
    : AbstractTestRunResultListener(), AttachmentListener {

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
        val results = runResult.testResults
        val tests = testBatch.tests.associateBy { it.identifier() }

        val testResults = runResult.testResults.map {
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

        val skipped = tests.filterNot {
            results.containsKey(it.key)
        }.values

        if (skipped.isNotEmpty()) {
            skipped.forEach {
                logger.warn { "skipped = ${it.toTestName()}, ${device.serialNumber}" }
            }
        }

        deferred.complete(TestBatchResults(device, finished, failed, skipped))
    }

    fun Map.Entry<TestIdentifier, DdmLibTestResult>.toTestResult(device: Device): TestResult {
        val testInstanceFromBatch = testBatch.tests.find { "${it.pkg}.${it.clazz}" == key.className && it.method == key.testName }
        val test = key.toTest()
        val attachments = attachments[test] ?: emptyList<Attachment>()
        return TestResult(test = testInstanceFromBatch ?: test,
                device = device.toDeviceInfo(),
                status = value.status.toMarathonStatus(),
                startTime = value.startTime,
                endTime = value.endTime,
                stacktrace = value.stackTrace,
                attachments = attachments)
    }


    private fun Test.identifier(): TestIdentifier {
        return TestIdentifier("$pkg.$clazz", method)
    }

    private fun DdmLibTestResult.isSuccessful() =
            status == DdmLibTestResult.TestStatus.PASSED ||
                    status == DdmLibTestResult.TestStatus.IGNORED ||
                    status == DdmLibTestResult.TestStatus.ASSUMPTION_FAILURE

}
