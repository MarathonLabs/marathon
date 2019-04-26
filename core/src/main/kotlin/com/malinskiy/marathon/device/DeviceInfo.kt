package com.malinskiy.marathon.device

data class DeviceInfo(val operatingSystem: OperatingSystem,
                 val serialNumber: String,
                 val model: String,
                 val manufacturer: String,
                 val networkState: NetworkState,
                 val deviceFeatures: Collection<DeviceFeature>,
                 val healthy: Boolean)
