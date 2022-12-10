package com.malinskiy.marathon.ios.executor.listener

import com.malinskiy.marathon.execution.result.TestRunResultsAccumulator
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.time.Timer

abstract class AccumulatingTestResultListener(private val expectedTestCount: Int, timer: Timer) : AppleTestRunListener {
    protected val runResult = TestRunResultsAccumulator(timer)

    override suspend fun testRunStarted() {
        runResult.testRunStarted("apple", expectedTestCount)
    }

    override suspend fun testStarted(test: Test) {
        runResult.testStarted(test)
    }

    override suspend fun testFailed(test: Test, startTime: Long, endTime: Long) {
        runResult.testFailed(test = test, trace = "", startTime = startTime, endTime = endTime)
    }

    override suspend fun testPassed(test: Test, startTime: Long, endTime: Long) {
        runResult.testEnded(test = test, testMetrics = emptyMap(), startTime = startTime, endTime = endTime)
    }

    override suspend fun testRunEnded() {
        runResult.testRunEnded(0, emptyMap())
    }
}
