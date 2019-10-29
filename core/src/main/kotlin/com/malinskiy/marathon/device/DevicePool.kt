package com.malinskiy.marathon.device

data class DevicePool(
    val name: String,
    var devices: Collection<Device>
)
