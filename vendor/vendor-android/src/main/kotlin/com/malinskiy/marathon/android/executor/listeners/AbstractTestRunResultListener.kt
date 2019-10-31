package com.malinskiy.marathon.android.executor.listeners

import com.malinskiy.marathon.android.executor.toTestIdentifier
import com.malinskiy.marathon.test.Test
import com.android.ddmlib.testrunner.TestRunResult as DdmLibTestRunResult

abstract class AbstractTestRunResultListener : TestRunListener {

    private val runResult: DdmLibTestRunResult = DdmLibTestRunResult()

    override fun testRunStarted(runName: String, testCount: Int) {
        runResult.testRunStarted(runName, testCount)
    }

    override fun testStarted(test: Test) {
        runResult.testStarted(test.toTestIdentifier())
    }

    override fun testFailed(test: Test, trace: String) {
        runResult.testFailed(test.toTestIdentifier(), trace)
    }

    override fun testAssumptionFailure(test: Test, trace: String) {
        runResult.testAssumptionFailure(test.toTestIdentifier(), trace)
    }

    override fun testIgnored(test: Test) {
        runResult.testIgnored(test.toTestIdentifier())
    }

    override fun testEnded(test: Test, testMetrics: Map<String, String>) {
        runResult.testEnded(test.toTestIdentifier(), testMetrics)
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

    abstract fun handleTestRunResults(runResult: DdmLibTestRunResult)
}
