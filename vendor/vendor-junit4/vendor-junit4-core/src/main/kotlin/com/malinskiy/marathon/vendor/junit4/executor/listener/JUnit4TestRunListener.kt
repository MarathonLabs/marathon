package com.malinskiy.marathon.vendor.junit4.executor.listener

import com.malinskiy.marathon.vendor.junit4.model.TestIdentifier

interface JUnit4TestRunListener {
    suspend fun testRunStarted(runName: String, testCount: Int) {}

    suspend fun testStarted(test: TestIdentifier) {}

    suspend fun testFailed(test: TestIdentifier, trace: String) {}

    suspend fun testAssumptionFailure(test: TestIdentifier, trace: String) {}

    suspend fun testIgnored(test: TestIdentifier) {}

    suspend fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {}

    suspend fun testRunFailed(errorMessage: String) {}

    suspend fun testRunStopped(elapsedTime: Long) {}

    suspend fun testRunEnded(elapsedTime: Long, runMetrics: Map<String, String>) {}
}
