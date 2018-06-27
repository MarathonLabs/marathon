package com.malinskiy.marathon.analytics.tracker

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult

internal open class NoOpTracker : Tracker {
    override fun terminate() {}

    override fun trackTestResult(poolId: DevicePoolId, device: Device, testResult: TestResult) {
    }

    override fun trackDeviceConnected(poolId: DevicePoolId, device: Device) {
    }
}
