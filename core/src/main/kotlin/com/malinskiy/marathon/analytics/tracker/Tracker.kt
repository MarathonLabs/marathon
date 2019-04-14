package com.malinskiy.marathon.analytics.tracker

import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult

interface Tracker {
    fun trackRawTestRun(poolId: DevicePoolId, device: DeviceInfo, testResult: TestResult)

    fun trackTestFinished(poolId: DevicePoolId, device: DeviceInfo, testResult: TestResult)

    fun trackDeviceConnected(poolId: DevicePoolId, device: DeviceInfo)

    fun terminate()
}
