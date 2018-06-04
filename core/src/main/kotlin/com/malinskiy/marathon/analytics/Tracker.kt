package com.malinskiy.marathon.analytics

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.test.Test

interface Tracker {
    fun trackTestStarted(test: Test, time: Int)
    fun trackTestResult(poolId: DevicePoolId, device: Device, testResult: TestResult)
    fun trackTestIgnored(test: Test)

    fun trackDeviceConnected(poolId: DevicePoolId, device: Device)
}
