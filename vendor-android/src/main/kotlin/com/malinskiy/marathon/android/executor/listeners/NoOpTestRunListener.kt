package com.malinskiy.marathon.android.executor.listeners

import com.android.ddmlib.testrunner.ITestRunListener
import com.android.ddmlib.testrunner.TestIdentifier

open class NoOpTestRunListener : ITestRunListener {
    override fun testRunStarted(runName: String, testCount: Int) {}

    override fun testStarted(test: TestIdentifier) {}

    override fun testFailed(test: TestIdentifier, trace: String) {}

    override fun testAssumptionFailure(test: TestIdentifier, trace: String) {}

    override fun testIgnored(test: TestIdentifier) {}

    override fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {}

    override fun testRunFailed(errorMessage: String) {}

    override fun testRunStopped(elapsedTime: Long) {}

    override fun testRunEnded(elapsedTime: Long, runMetrics: Map<String, String>) {}
}
