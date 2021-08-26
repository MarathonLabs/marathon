package com.malinskiy.marathon.vendor.junit4.executor.listener

import com.malinskiy.marathon.vendor.junit4.model.TestIdentifier


class CompositeTestRunListener(private val listeners: List<JUnit4TestRunListener>) : JUnit4TestRunListener {
    private inline fun execute(f: (JUnit4TestRunListener) -> Unit) {
        listeners.forEach(f)
    }

    override suspend fun testRunStarted(runName: String, testCount: Int) {
        execute { it.testRunStarted(runName, testCount) }
    }

    override suspend fun testStarted(test: TestIdentifier) {
        execute { it.testStarted(test) }
    }

    override suspend fun testAssumptionFailure(test: TestIdentifier, trace: String) {
        execute { it.testAssumptionFailure(test, trace) }
    }

    override suspend fun testRunStopped(elapsedTime: Long) {
        execute { it.testRunStopped(elapsedTime) }
    }

    override suspend fun testFailed(test: TestIdentifier, trace: String) {
        execute { it.testFailed(test, trace) }
    }

    override suspend fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {
        execute { it.testEnded(test, testMetrics) }
    }

    override suspend fun testIgnored(test: TestIdentifier) {
        execute { it.testIgnored(test) }
    }

    override suspend fun testRunFailed(errorMessage: String) {
        execute { it.testRunFailed(errorMessage) }
    }

    override suspend fun testRunEnded(elapsedTime: Long, runMetrics: Map<String, String>) {
        execute { it.testRunEnded(elapsedTime, runMetrics) }
    }
}
