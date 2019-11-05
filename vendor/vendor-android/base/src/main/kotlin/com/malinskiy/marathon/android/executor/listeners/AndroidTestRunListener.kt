package com.malinskiy.marathon.android.executor.listeners

import com.malinskiy.marathon.android.model.TestIdentifier

interface AndroidTestRunListener {
    fun testRunStarted(runName: String, testCount: Int) {}

    fun testStarted(test: TestIdentifier) {}

    fun testFailed(test: TestIdentifier, trace: String) {}

    fun testAssumptionFailure(test: TestIdentifier, trace: String) {}

    fun testIgnored(test: TestIdentifier) {}

    fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {}

    fun testRunFailed(errorMessage: String) {}

    fun testRunStopped(elapsedTime: Long) {}

    fun testRunEnded(elapsedTime: Long, runMetrics: Map<String, String>) {}
}