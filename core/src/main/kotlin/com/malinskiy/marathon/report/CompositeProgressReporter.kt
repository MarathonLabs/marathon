package com.malinskiy.marathon.report

import com.malinskiy.marathon.analytics.internal.sub.ExecutionReport
import com.malinskiy.marathon.test.Test

class CompositeProgressReporter(private val delegates: Collection<ProgressReporter>) : ProgressReporter {
    override fun begin(parsedFilteredTests: List<Test>) {
        delegates.forEach { it.begin(parsedFilteredTests) }
    }

    override fun testStarted(progress: Float, poolId: String, deviceSerial: String, testName: String) {
        delegates.forEach { it.testStarted(progress, poolId, deviceSerial, testName) }
    }

    override fun testFailed(progress: Float, poolId: String, deviceSerial: String, testName: String) {
        delegates.forEach { it.testFailed(progress, poolId, deviceSerial, testName) }
    }

    override fun testPassed(progress: Float, poolId: String, deviceSerial: String, testName: String) {
        delegates.forEach { it.testPassed(progress, poolId, deviceSerial, testName) }
    }

    override fun testIgnored(progress: Float, poolId: String, deviceSerial: String, testName: String) {
        delegates.forEach { it.testIgnored(progress, poolId, deviceSerial, testName) }
    }

    override fun testIncomplete(progress: Float, poolId: String, deviceSerial: String, testName: String) {
        delegates.forEach { it.testIncomplete(progress, poolId, deviceSerial, testName) }
    }

    override fun end(executionReport: ExecutionReport) {
        delegates.forEach { it.end(executionReport) }
    }
}
