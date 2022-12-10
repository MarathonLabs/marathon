package com.malinskiy.marathon.ios.executor.listener

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.test.Test

class ProgressReportingListener(
    device: Device,
    private val poolId: DevicePoolId,
    private val progressReporter: ProgressReporter,
) : AppleTestRunListener {
    private val deviceInfo = device.toDeviceInfo()

    override suspend fun testStarted(test: Test) {
        progressReporter.testStarted(poolId, deviceInfo, test)
    }

    override suspend fun testFailed(test: Test, startTime: Long, endTime: Long) {
        progressReporter.testFailed(poolId, deviceInfo, test)
    }

    override suspend fun testPassed(test: Test, startTime: Long, endTime: Long) {
        progressReporter.testPassed(poolId, deviceInfo, test)
    }
}
