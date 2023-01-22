package com.malinskiy.marathon.ios.executor.listener

import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.test.Test

class ProgressReportingListener(
    private val deviceInfo: DeviceInfo,
    private val poolId: DevicePoolId,
    private val progressReporter: ProgressReporter,
) : AppleTestRunListener {
    override suspend fun testStarted(test: Test) {
        progressReporter.testStarted(poolId, deviceInfo, test)
    }

    override suspend fun testFailed(test: Test, startTime: Long, endTime: Long, trace: String?) {
        progressReporter.testFailed(poolId, deviceInfo, test)
    }

    override suspend fun testPassed(test: Test, startTime: Long, endTime: Long) {
        progressReporter.testPassed(poolId, deviceInfo, test)
    }
}
