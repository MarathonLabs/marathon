package com.malinskiy.marathon.ios.simctl.model

data class SimctlListDevicesOutput(val devicetypes: List<SimctlDeviceType>,
                                   val runtimes: List<SimctlRuntime>,
                                   val devices: SimctlDeviceList)
