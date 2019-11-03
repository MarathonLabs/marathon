package com.malinskiy.marathon.android.executor.listeners

import com.malinskiy.marathon.android.model.TestRunResult
import com.malinskiy.marathon.test.Test

abstract class AbstractTestRunResultListener : NoOpTestRunListener() {
    private val runResult = TestRunResult()

    override fun testRunStarted(runName: String, testCount: Int) {
        runResult.testRunStarted(runName, testCount)
    }

    override fun testStarted(test: Test) {
        runResult.testStarted(test)
    }

    override fun testFailed(test: Test, trace: String) {
        runResult.testFailed(test, trace)
    }

    override fun testAssumptionFailure(test: Test, trace: String) {
        runResult.testAssumptionFailure(test, trace)
    }

    override fun testIgnored(test: Test) {
        runResult.testIgnored(test)
    }

    override fun testEnded(test: Test, testMetrics: Map<String, String>) {
        runResult.testEnded(test, testMetrics)
    }

    override fun testRunFailed(errorMessage: String) {
        runResult.testRunFailed(errorMessage)
        handleTestRunResults(runResult)
    }

    override fun testRunStopped(elapsedTime: Long) {
        runResult.testRunStopped(elapsedTime)
        handleTestRunResults(runResult)
    }

    override fun testRunEnded(elapsedTime: Long, runMetrics: Map<String, String>) {
        runResult.testRunEnded(elapsedTime, runMetrics)
        handleTestRunResults(runResult)
    }

    abstract fun handleTestRunResults(runResult: TestRunResult)
}
