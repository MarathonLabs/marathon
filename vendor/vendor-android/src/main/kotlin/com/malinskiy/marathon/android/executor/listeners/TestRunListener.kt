package com.malinskiy.marathon.android.executor.listeners

import com.malinskiy.marathon.test.Test

interface TestRunListener {
    fun testRunStarted(runName: String, testCount: Int) {}

    fun testStarted(test: Test) {}

    fun testFailed(test: Test, trace: String) {}

    fun testAssumptionFailure(test: Test, trace: String) {}

    fun testIgnored(test: Test) {}

    fun testEnded(test: Test, testMetrics: Map<String, String>) {}

    fun testRunFailed(errorMessage: String) {}

    fun testRunStopped(elapsedTime: Long) {}

    fun testRunEnded(elapsedTime: Long, runMetrics: Map<String, String>) {}
}
