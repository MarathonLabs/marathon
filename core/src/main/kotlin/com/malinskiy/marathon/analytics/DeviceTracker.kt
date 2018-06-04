package com.malinskiy.marathon.analytics

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.report.internal.DeviceInfoSerializer

class DeviceTracker(private val deviceInfoSerializer: DeviceInfoSerializer) : NoOpTracker() {
    override fun trackDeviceConnected(poolId: DevicePoolId, device: Device) {
        deviceInfoSerializer.deviceConnected(poolId, device)
    }
}
