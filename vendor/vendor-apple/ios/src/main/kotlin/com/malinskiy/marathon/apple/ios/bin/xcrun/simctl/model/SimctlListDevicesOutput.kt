package com.malinskiy.marathon.apple.ios.bin.xcrun.simctl.model

data class SimctlListDevicesOutput(
    val devicetypes: List<SimctlDeviceType>,
    val runtimes: List<SimctlRuntime>,
    val devices: SimctlDeviceList
) {
    val supportedDeviceTypeIds = lazy { 
        
    }
}
