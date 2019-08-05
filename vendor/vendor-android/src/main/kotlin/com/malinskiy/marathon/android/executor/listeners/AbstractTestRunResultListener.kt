package com.malinskiy.marathon.android.executor.listeners

import com.android.ddmlib.testrunner.TestIdentifier
import com.malinskiy.marathon.execution.TestBatchResults
import com.android.ddmlib.testrunner.TestRunResult as DdmLibTestRunResult

abstract class AbstractTestRunResultListener() : NoOpTestRunListener() {

    val runResult: DdmLibTestRunResult = DdmLibTestRunResult()

    private var active = true

    override fun testRunStarted(runName: String, testCount: Int) {
        synchronized(runResult) {
            if (active) runResult.testRunStarted(runName, testCount)
        }
    }

    override fun testStarted(test: TestIdentifier) {
        synchronized(runResult) {
            if (active) runResult.testStarted(test)
        }
    }

    override fun testFailed(test: TestIdentifier, trace: String) {
        synchronized(runResult) {
            if (active) runResult.testFailed(test, trace)
        }
    }

    override fun testAssumptionFailure(test: TestIdentifier, trace: String) {
        synchronized(runResult) {
            if (active) runResult.testAssumptionFailure(test, trace)
        }
    }

    override fun testIgnored(test: TestIdentifier) {
        synchronized(runResult) {
            if (active) runResult.testIgnored(test)
        }
    }

    override fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {
        synchronized(runResult) {
            if (active) runResult.testEnded(test, testMetrics)
        }
    }

    override fun testRunFailed(errorMessage: String) {
        synchronized(runResult) {
            if (active) runResult.testRunFailed(errorMessage)
        }
    }

    override fun testRunStopped(elapsedTime: Long) {
        synchronized(runResult) {
            if (active) runResult.testRunStopped(elapsedTime)
        }
    }

    override fun testRunEnded(elapsedTime: Long, runMetrics: Map<String, String>) {
        synchronized(runResult) {
            if (active) {
                runResult.testRunEnded(elapsedTime, runMetrics)
            }
        }
    }

    abstract fun getResults(): TestBatchResults
}
