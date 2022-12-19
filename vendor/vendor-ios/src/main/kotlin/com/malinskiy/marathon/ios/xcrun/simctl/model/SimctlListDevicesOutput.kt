package com.malinskiy.marathon.ios.xcrun.simctl.model

data class SimctlListDevicesOutput(
    val devicetypes: List<SimctlDeviceType>,
    val runtimes: List<SimctlRuntime>,
    val devices: SimctlDeviceList
)
