package com.malinskiy.marathon.lite.configuration

enum class DeviceFeature {
    VIDEO,
    SCREENSHOT;
}

fun DeviceFeature.toProto(): com.malinskiy.marathon.cliconfig.proto.DeviceFeature {
    return when (this) {
        DeviceFeature.SCREENSHOT -> com.malinskiy.marathon.cliconfig.proto.DeviceFeature.SCREENSHOT
        DeviceFeature.VIDEO -> com.malinskiy.marathon.cliconfig.proto.DeviceFeature.VIDEO
    }
}
