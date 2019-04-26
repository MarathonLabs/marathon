package com.malinskiy.marathon.device

data class DeviceInfo(val operatingSystem: OperatingSystem,
                 val serialNumber: String,
                 val model: String,
                 val manufacturer: String,
                 val networkState: NetworkState,
                 val deviceFeatures: Collection<DeviceFeature>,
                 val healthy: Boolean,
                 val deviceLabel: String) {
    constructor(operatingSystem: OperatingSystem,
                serialNumber: String,
                model: String,
                manufacturer: String,
                networkState: NetworkState,
                deviceFeatures: Collection<DeviceFeature>,
                healthy: Boolean): this(
            operatingSystem = operatingSystem,
            serialNumber = serialNumber,
            model = model,
            manufacturer = manufacturer,
            networkState = networkState,
            deviceFeatures = deviceFeatures,
            healthy = healthy,
            deviceLabel = serialNumber
    )
}
