package com.malinskiy.marathon.android.executor.listeners

import com.android.ddmlib.testrunner.TestIdentifier
import com.android.ddmlib.testrunner.TestResult
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.experimental.CompletableDeferred
import com.android.ddmlib.testrunner.TestRunResult as DdmLibTestRunResult

class TestRunResultsListener(private val testBatch: TestBatch,
                             private val device: Device,
                             private val deferred: CompletableDeferred<TestBatchResults>) : AbstractTestRunResultListener() {
    override fun handleTestRunResults(runResult: DdmLibTestRunResult) {
        val results = runResult.testResults
        val tests = testBatch.tests.associateBy { it.identifier() }

        val finished = tests.filter {
            results[it.key]?.isSuccessful() ?: false
        }.values

        val failed = tests.filterNot {
            results[it.key]?.isSuccessful() ?: false
        }.values

        deferred.complete(TestBatchResults(device, finished, failed))
    }


    private fun Test.identifier(): TestIdentifier {
        return TestIdentifier("$pkg.$clazz", method)
    }

    private fun TestResult.isSuccessful() =
            status == TestResult.TestStatus.PASSED || status == TestResult.TestStatus.IGNORED

}
