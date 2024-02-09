package com.malinskiy.marathon.apple.ios.listener

import com.malinskiy.marathon.apple.ios.logparser.parser.DeviceFailureReason
import com.malinskiy.marathon.test.Test

interface AppleTestRunListener {
    suspend fun beforeTestRun() {}
    suspend fun testRunStarted() {}
    suspend fun testStarted(test: Test) {}
    suspend fun testFailed(test: Test, startTime: Long, endTime: Long, trace: String?) {}
    suspend fun testPassed(test: Test, startTime: Long, endTime: Long) {}
    suspend fun testIgnored(test: Test, startTime: Long, endTime: Long) {}
    suspend fun testRunEnded() {}
    suspend fun testRunFailed(errorMessage: String, reason: DeviceFailureReason = DeviceFailureReason.Unknown) {}
    suspend fun afterTestRun() {}
}
