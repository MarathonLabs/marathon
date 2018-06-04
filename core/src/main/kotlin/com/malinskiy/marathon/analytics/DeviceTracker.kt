package com.malinskiy.marathon.analytics

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.report.internal.DeviceInfoReporter

class DeviceTracker(private val deviceInfoSerializer: DeviceInfoReporter) : NoOpTracker() {
    override fun trackDeviceConnected(poolId: DevicePoolId, device: Device) {
        deviceInfoSerializer.saveDeviceInfo(poolId, device)
    }
}
