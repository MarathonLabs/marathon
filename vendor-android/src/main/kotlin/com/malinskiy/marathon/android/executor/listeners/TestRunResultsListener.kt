package com.malinskiy.marathon.android.executor.listeners

import com.android.ddmlib.testrunner.TestIdentifier
import com.malinskiy.marathon.android.toMarathonStatus
import com.malinskiy.marathon.android.toTest
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import kotlinx.coroutines.experimental.CompletableDeferred
import com.android.ddmlib.testrunner.TestRunResult as DdmLibTestRunResult
import com.android.ddmlib.testrunner.TestResult as DdmLibTestResult

class TestRunResultsListener(private val testBatch: TestBatch,
                             private val device: Device,
                             private val deferred: CompletableDeferred<TestBatchResults>) : AbstractTestRunResultListener() {
    override fun handleTestRunResults(runResult: DdmLibTestRunResult) {
        val results = runResult.testResults
        val tests = testBatch.tests.associateBy { it.identifier() }

        val testResults = runResult.testResults.map {
            it.toTestResult(device)
        }

        val finished = testResults.filter {
            results[it.test.identifier()]?.isSuccessful() ?: false
        }

        val failed = testResults.filterNot {
            results[it.test.identifier()]?.isSuccessful() ?: false
        }

        val notExecuted = tests.filterNot {
            results.containsKey(it.key)
        }.values.map {
            TestResult(it, device.toDeviceInfo(), TestStatus.INCOMPLETE, 0, 0, null)
        }

        deferred.complete(TestBatchResults(device, finished, failed + notExecuted))
    }

    fun Map.Entry<TestIdentifier, DdmLibTestResult>.toTestResult(device: Device): TestResult {
        return TestResult(test = key.toTest(),
                device = device.toDeviceInfo(),
                status = value.status.toMarathonStatus(),
                startTime = value.startTime,
                endTime = value.endTime,
                stacktrace = value.stackTrace)
    }


    private fun Test.identifier(): TestIdentifier {
        return TestIdentifier("$pkg.$clazz", method)
    }

    private fun DdmLibTestResult.isSuccessful() =
            status == DdmLibTestResult.TestStatus.PASSED || status == DdmLibTestResult.TestStatus.IGNORED

}
