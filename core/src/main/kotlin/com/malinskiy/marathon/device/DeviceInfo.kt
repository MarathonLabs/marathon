package com.malinskiy.marathon.device

data class DeviceInfo(
    val operatingSystem: OperatingSystem,
    val serialNumber: String,
    val model: String,
    val manufacturer: String,
    val networkState: NetworkState,
    val deviceFeatures: Collection<DeviceFeature>,
    val healthy: Boolean
)

fun Device.toDeviceInfo() = DeviceInfo(
    operatingSystem = operatingSystem,
    serialNumber = serialNumber,
    model = model,
    manufacturer = manufacturer,
    networkState = networkState,
    deviceFeatures = deviceFeatures,
    healthy = healthy
)
