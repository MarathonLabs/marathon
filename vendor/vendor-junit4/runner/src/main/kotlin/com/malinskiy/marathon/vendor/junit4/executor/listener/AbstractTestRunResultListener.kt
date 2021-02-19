package com.malinskiy.marathon.vendor.junit4.executor.listener

import com.malinskiy.marathon.vendor.junit4.model.TestIdentifier
import com.malinskiy.marathon.vendor.junit4.model.TestRunResultsAccumulator

abstract class AbstractTestRunResultListener : NoOpTestRunListener() {
    private val runResult = TestRunResultsAccumulator()

    override suspend fun testRunStarted(runName: String, testCount: Int) {
        runResult.testRunStarted(runName, testCount)
    }

    override suspend fun testStarted(test: TestIdentifier) {
        runResult.testStarted(test)
    }

    override suspend fun testFailed(test: TestIdentifier, trace: String) {
        runResult.testFailed(test, trace)
    }

    override suspend fun testAssumptionFailure(test: TestIdentifier, trace: String) {
        runResult.testAssumptionFailure(test, trace)
    }

    override suspend fun testIgnored(test: TestIdentifier) {
        runResult.testIgnored(test)
    }

    override suspend fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {
        runResult.testEnded(test, testMetrics)
    }

    override suspend fun testRunFailed(errorMessage: String) {
        runResult.testRunFailed(errorMessage)
        handleTestRunResults(runResult)
    }

    override suspend fun testRunStopped(elapsedTime: Long) {
        runResult.testRunStopped(elapsedTime)
        handleTestRunResults(runResult)
    }

    override suspend fun testRunEnded(elapsedTime: Long, runMetrics: Map<String, String>) {
        runResult.testRunEnded(elapsedTime, runMetrics)
        handleTestRunResults(runResult)
    }

    abstract suspend fun handleTestRunResults(runResult: TestRunResultsAccumulator)
}
