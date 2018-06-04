package com.malinskiy.marathon.analytics

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.test.Test

open class NoOpTracker : Tracker{
    override fun trackTestStarted(test: Test, time: Int) {
    }

    override fun trackTestResult(poolId: DevicePoolId, device: Device, testResult: TestResult) {
    }

    override fun trackTestIgnored(test: Test) {
    }

    override fun trackDeviceConnected(poolId: DevicePoolId, device: Device) {
    }
}
