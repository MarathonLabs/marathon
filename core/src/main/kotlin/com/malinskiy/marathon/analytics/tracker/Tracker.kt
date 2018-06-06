package com.malinskiy.marathon.analytics.tracker

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult

interface Tracker {
    fun trackTestResult(poolId: DevicePoolId, device: Device, testResult: TestResult)

    fun trackDeviceConnected(poolId: DevicePoolId, device: Device)
}
