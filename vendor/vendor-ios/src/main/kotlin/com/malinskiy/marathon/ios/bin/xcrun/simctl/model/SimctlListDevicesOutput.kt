package com.malinskiy.marathon.ios.bin.xcrun.simctl.model

data class SimctlListDevicesOutput(
    val devicetypes: List<SimctlDeviceType>,
    val runtimes: List<SimctlRuntime>,
    val devices: SimctlDeviceList
) {
    val supportedDeviceTypeIds = lazy { 
        
    }
}
