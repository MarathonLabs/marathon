package com.malinskiy.marathon.analytics

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.test.Test

interface Tracker {
    fun trackTestStarted(test: Test, time: Int)
    fun trackTestFinished(test: Test, success: Boolean, time: Int)
    fun trackTestIgnored(test: Test)

    fun trackTestResult(poolId: DevicePoolId, device: Device, testResult: TestResult)
    fun trackDeviceConnected(poolId: DevicePoolId, device: Device)

    fun calculateTestExpectedTime(test: Test) : Int
    fun calculateTestVariance(test: Test) : Int
    fun calculateTestExpectedRetries(test: Test) : Int
}
