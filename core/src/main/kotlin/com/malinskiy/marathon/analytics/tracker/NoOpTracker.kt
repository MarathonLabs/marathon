package com.malinskiy.marathon.analytics.tracker

import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult

open class NoOpTracker : Tracker {
    override fun terminate() {}

    override fun trackRawTestRun(poolId: DevicePoolId, device: DeviceInfo, testResult: TestResult) {}

    override fun trackTestFinished(poolId: DevicePoolId, device: DeviceInfo, testResult: TestResult) {}

    override fun trackDeviceConnected(poolId: DevicePoolId, device: DeviceInfo) {}
}
