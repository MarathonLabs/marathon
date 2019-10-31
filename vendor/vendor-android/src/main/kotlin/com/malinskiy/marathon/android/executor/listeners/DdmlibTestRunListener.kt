package com.malinskiy.marathon.android.executor.listeners

import com.android.ddmlib.testrunner.ITestRunListener
import com.android.ddmlib.testrunner.TestIdentifier
import com.malinskiy.marathon.android.toTest
import com.malinskiy.marathon.execution.ComponentInfo

class DdmlibTestRunListener(
    private val componentInfo: ComponentInfo,
    private val delegate: TestRunListener
) : ITestRunListener {

    override fun testRunStarted(runName: String, testCount: Int) {
        delegate.testRunStarted(runName, testCount)
    }

    override fun testStarted(test: TestIdentifier) {
        delegate.testStarted(test.toTest(componentInfo))
    }

    override fun testAssumptionFailure(test: TestIdentifier, trace: String) {
        delegate.testAssumptionFailure(test.toTest(componentInfo), trace)
    }

    override fun testRunStopped(elapsedTime: Long) {
        delegate.testRunStopped(elapsedTime)
    }

    override fun testFailed(test: TestIdentifier, trace: String) {
        delegate.testFailed(test.toTest(componentInfo), trace)
    }

    override fun testEnded(test: TestIdentifier, testMetrics: MutableMap<String, String>) {
        delegate.testEnded(test.toTest(componentInfo), testMetrics)
    }

    override fun testIgnored(test: TestIdentifier) {
        delegate.testIgnored(test.toTest(componentInfo))
    }

    override fun testRunFailed(errorMessage: String) {
        delegate.testRunFailed(errorMessage)
    }

    override fun testRunEnded(elapsedTime: Long, runMetrics: MutableMap<String, String>) {
        delegate.testRunEnded(elapsedTime, runMetrics)
    }
}