package com.malinskiy.marathon.apple.model

enum class Sdk(val value: String) {
    IPHONEOS("iphoneos"),
    IPHONESIMULATOR("iphonesimulator");
    
    val platformName: String by lazy { 
        when(this) {
            IPHONEOS -> "iPhoneOS"
            IPHONESIMULATOR -> "iPhoneSimulator"
        }
    }
}
