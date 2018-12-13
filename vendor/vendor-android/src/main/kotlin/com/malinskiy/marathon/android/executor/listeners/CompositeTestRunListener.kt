package com.malinskiy.marathon.android.executor.listeners

import com.android.ddmlib.testrunner.ITestRunListener
import com.android.ddmlib.testrunner.TestIdentifier

class CompositeTestRunListener(private val listeners: List<ITestRunListener>) : ITestRunListener {
    private inline fun execute(f: (ITestRunListener) -> Unit) {
        listeners.forEach(f)
    }

    override fun testRunStarted(runName: String?, testCount: Int) {
        execute { it.testRunStarted(runName, testCount) }
    }

    override fun testStarted(test: TestIdentifier?) {
        execute { it.testStarted(test) }
    }

    override fun testAssumptionFailure(test: TestIdentifier?, trace: String?) {
        execute { it.testAssumptionFailure(test, trace) }
    }

    override fun testRunStopped(elapsedTime: Long) {
        execute { it.testRunStopped(elapsedTime) }
    }

    override fun testFailed(test: TestIdentifier?, trace: String?) {
        execute { it.testFailed(test, trace) }
    }

    override fun testEnded(test: TestIdentifier?, testMetrics: MutableMap<String, String>?) {
        execute { it.testEnded(test, testMetrics) }
    }

    override fun testIgnored(test: TestIdentifier?) {
        execute { it.testIgnored(test) }
    }

    override fun testRunFailed(errorMessage: String?) {
        execute { it.testRunFailed(errorMessage) }
    }

    override fun testRunEnded(elapsedTime: Long, runMetrics: MutableMap<String, String>?) {
        execute { it.testRunEnded(elapsedTime, runMetrics) }
    }
}
