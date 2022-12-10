package com.malinskiy.marathon.ios.executor.listener

import com.malinskiy.marathon.test.Test

interface AppleTestRunListener {
    suspend fun beforeTestRun() {}
    suspend fun testRunStarted() {}
    suspend fun testStarted(test: Test) {}
    suspend fun testFailed(test: Test, startTime: Long, endTime: Long) {}
    suspend fun testPassed(test: Test, startTime: Long, endTime: Long) {}
    suspend fun testRunEnded() {}
    suspend fun afterTestRun() {}
}
