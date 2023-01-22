package com.malinskiy.marathon.android.executor.listeners

import com.malinskiy.marathon.android.model.TestIdentifier
import com.malinskiy.marathon.execution.result.TestRunResultsAccumulator
import com.malinskiy.marathon.time.Timer

abstract class AccumulatingResultTestRunListener(timer: Timer) : AndroidTestRunListener {
    protected val runResult = TestRunResultsAccumulator(timer)

    override suspend fun testRunStarted(runName: String, testCount: Int) {
        runResult.testRunStarted(runName, testCount)
    }

    override suspend fun testStarted(test: TestIdentifier) {
        runResult.testStarted(test.toTest())
    }

    override suspend fun testFailed(test: TestIdentifier, trace: String) {
        runResult.testFailed(test.toTest(), trace)
    }

    override suspend fun testAssumptionFailure(test: TestIdentifier, trace: String) {
        runResult.testAssumptionFailure(test.toTest(), trace)
    }

    override suspend fun testIgnored(test: TestIdentifier) {
        runResult.testIgnored(test.toTest())
    }

    override suspend fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {
        runResult.testEnded(test.toTest(), testMetrics)
    }

    override suspend fun testRunFailed(errorMessage: String) {
        runResult.testRunFailed(errorMessage)
    }

    override suspend fun testRunStopped(elapsedTime: Long) {
        runResult.testRunStopped(elapsedTime)
    }

    override suspend fun testRunEnded(elapsedTime: Long, runMetrics: Map<String, String>) {
        runResult.testRunEnded(elapsedTime, runMetrics)
    }
}
