package com.malinskiy.marathon.ios.executor.listener

import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureReason
import com.malinskiy.marathon.test.Test

class CompositeTestRunListener(private val listeners: List<AppleTestRunListener>) : AppleTestRunListener {
    private inline fun execute(f: (AppleTestRunListener) -> Unit) {
        listeners.forEach(f)
    }

    override suspend fun beforeTestRun() {
        execute { it.beforeTestRun() }
    }

    override suspend fun testRunStarted() {
        execute { it.testRunStarted() }
    }

    override suspend fun testStarted(test: Test) {
        execute { it.testStarted(test) }
    }

    override suspend fun testFailed(test: Test, startTime: Long, endTime: Long, trace: String?) {
        execute { it.testFailed(test, startTime, endTime, trace) }
    }

    override suspend fun testPassed(test: Test, startTime: Long, endTime: Long) {
        execute { it.testPassed(test, startTime, endTime) }
    }

    override suspend fun testIgnored(test: Test, startTime: Long, endTime: Long) {
        execute { it.testIgnored(test, startTime, endTime) }
    }

    override suspend fun testRunFailed(errorMessage: String, reason: DeviceFailureReason) {
        execute { it.testRunFailed(errorMessage,) }
    }

    override suspend fun testRunEnded() {
        execute { it.testRunEnded() }
    }

    override suspend fun afterTestRun() {
        execute { it.afterTestRun() }
    }
}
