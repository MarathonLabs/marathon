package com.malinskiy.marathon.ios.executor.listener

import com.malinskiy.marathon.execution.listener.TestRunListener
import com.malinskiy.marathon.test.Test

/**
 * Apple doesn't have test ended event, so we simulate it when receiving pass/fail
 */
class TestRunListenerAdapter(private val listener: TestRunListener) : AppleTestRunListener {
    override suspend fun beforeTestRun() {
        listener.beforeTestRun()
    }

    override suspend fun testStarted(test: Test) {
        listener.testStarted(test)
    }

    override suspend fun testFailed(test: Test, startTime: Long, endTime: Long, trace: String?) {
        listener.testEnded(test)
    }

    override suspend fun testPassed(test: Test, startTime: Long, endTime: Long) {
        listener.testEnded(test)
    }

    override suspend fun afterTestRun() {
        listener.afterTestRun()
    }
}
