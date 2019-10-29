package com.malinskiy.marathon.android.executor.listeners

import com.malinskiy.marathon.test.Test

open class NoOpTestRunListener : AndroidTestRunListener {
    override fun testRunStarted(runName: String, testCount: Int) {}

    override fun testStarted(test: Test) {}

    override fun testFailed(test: Test, trace: String) {}

    override fun testAssumptionFailure(test: Test, trace: String) {}

    override fun testIgnored(test: Test) {}

    override fun testEnded(test: Test, testMetrics: Map<String, String>) {}

    override fun testRunFailed(errorMessage: String) {}

    override fun testRunStopped(elapsedTime: Long) {}

    override fun testRunEnded(elapsedTime: Long, runMetrics: Map<String, String>) {}
}
