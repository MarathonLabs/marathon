package com.malinskiy.marathon

import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.device.OperatingSystem


fun createDeviceInfo(
    operatingSystem: OperatingSystem = OperatingSystem("Fake OS"),
    serialNumber: String = "fake serial",
    model: String = "fake model",
    manufacturer: String = "fake manufacturer",
    networkState: NetworkState = NetworkState.CONNECTED,
    deviceFeatures: Collection<DeviceFeature> = emptyList(),
    healthy: Boolean = true
): DeviceInfo {
    return DeviceInfo(operatingSystem, serialNumber, model, manufacturer, networkState, deviceFeatures, healthy)
}