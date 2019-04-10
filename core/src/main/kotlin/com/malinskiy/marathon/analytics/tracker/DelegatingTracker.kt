package com.malinskiy.marathon.analytics.tracker

import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult

internal class DelegatingTracker(private val trackers: List<Tracker>) : Tracker {
    override fun trackRawTestRun(poolId: DevicePoolId, device: DeviceInfo, testResult: TestResult) {
        trackers.forEach {
            it.trackRawTestRun(poolId, device, testResult)
        }
    }

    override fun trackTestFinished(poolId: DevicePoolId, device: DeviceInfo, testResult: TestResult) {
        trackers.forEach {
            it.trackTestFinished(poolId, device, testResult)
        }
    }

    override fun terminate() {
        trackers.forEach {
            it.terminate()
        }
    }

    override fun trackDeviceConnected(poolId: DevicePoolId, device: DeviceInfo) {
        trackers.forEach {
            it.trackDeviceConnected(poolId, device)
        }
    }
}
