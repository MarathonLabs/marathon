package com.malinskiy.marathon.analytics.tracker

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.test.Test

internal class DelegatingTracker(private val trackers: List<Tracker>) : Tracker {
    override fun trackTestStarted(test: Test, time: Int) {
        trackers.forEach {
            it.trackTestStarted(test, time)
        }
    }

    override fun trackTestResult(poolId: DevicePoolId, device: Device, testResult: TestResult) {
        trackers.forEach {
            it.trackTestResult(poolId, device, testResult)
        }
    }

    override fun trackTestIgnored(test: Test) {
        trackers.forEach {
            it.trackTestIgnored(test)
        }
    }

    override fun trackDeviceConnected(poolId: DevicePoolId, device: Device) {
        trackers.forEach {
            it.trackDeviceConnected(poolId, device)
        }
    }
}
