package com.malinskiy.marathon.report

import com.malinskiy.marathon.analytics.internal.sub.ExecutionReport
import com.malinskiy.marathon.test.Test

interface ProgressReporter {
    fun begin(parsedFilteredTests: List<Test>) = Unit

    fun testStarted(progress: Float, poolId: String, deviceSerial: String, testName: String) = Unit
    fun testFailed(progress: Float, poolId: String, deviceSerial: String, testName: String) = Unit
    fun testPassed(progress: Float, poolId: String, deviceSerial: String, testName: String) = Unit
    fun testIgnored(progress: Float, poolId: String, deviceSerial: String, testName: String) = Unit
    fun testIncomplete(progress: Float, poolId: String, deviceSerial: String, testName: String) = Unit

    fun end(executionReport: ExecutionReport) = Unit
}
