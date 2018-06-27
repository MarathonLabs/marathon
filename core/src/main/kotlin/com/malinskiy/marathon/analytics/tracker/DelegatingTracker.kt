package com.malinskiy.marathon.analytics.tracker

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult

internal class DelegatingTracker(private val trackers: List<Tracker>) : Tracker {
    override fun terminate() {
        trackers.forEach {
            it.terminate()
        }
    }

    override fun trackTestResult(poolId: DevicePoolId, device: Device, testResult: TestResult) {
        trackers.forEach {
            it.trackTestResult(poolId, device, testResult)
        }
    }

    override fun trackDeviceConnected(poolId: DevicePoolId, device: Device) {
        trackers.forEach {
            it.trackDeviceConnected(poolId, device)
        }
    }
}
