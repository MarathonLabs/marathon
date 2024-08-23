package com.malinskiy.marathon.apple.listener

import com.malinskiy.marathon.apple.listener.AppleTestRunListener
import com.malinskiy.marathon.apple.logparser.parser.DeviceFailureException
import com.malinskiy.marathon.apple.logparser.parser.DeviceFailureReason
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

    override suspend fun testFailed(test: Test, startTime: Long, endTime: Long, trace: String?) {
        runResult.testFailed(test = test, trace = trace ?: "", startTime = startTime, endTime = endTime)
        testEnded(test, startTime, endTime)
    }

    override suspend fun testPassed(test: Test, startTime: Long, endTime: Long) {
        testEnded(test, startTime, endTime)
    }

    override suspend fun testIgnored(test: Test, startTime: Long, endTime: Long) {
        runResult.testIgnored(test)
        testEnded(test, startTime, endTime)
    }

    /**
     * Even though apple provides the timestamps for tests, there is no expectation of these being in sync with local time
     */
    private fun testEnded(test: Test, startTime: Long, endTime: Long) {
        runResult.testEnded(test = test, testMetrics = emptyMap())
    }

    override suspend fun testRunFailed(errorMessage: String, reason: DeviceFailureReason) {
        runResult.testRunFailed(errorMessage)
        //TODO: check how we handle these
        throw DeviceFailureException(reason, errorMessage)
    }

    override suspend fun testRunEnded() {
        runResult.testRunEnded(0, emptyMap())
    }
}
