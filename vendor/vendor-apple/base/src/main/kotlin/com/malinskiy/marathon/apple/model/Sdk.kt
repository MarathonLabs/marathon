package com.malinskiy.marathon.apple.model

enum class Sdk(val value: String) {
    IPHONEOS("iphoneos"),
    IPHONESIMULATOR("iphonesimulator"),
    MACOS("macosx");
    
    val platformName: String by lazy { 
        when(this) {
            IPHONEOS -> "iPhoneOS"
            IPHONESIMULATOR -> "iPhoneSimulator"
            MACOS -> "MacOSX"
        }
    }

    /**
     * destination platform for xcodebuild argument
     */
    val destination: String by lazy {
        when(this) {
            IPHONEOS -> "iOS"
            IPHONESIMULATOR -> "iOS Simulator"
            MACOS -> "OS X"
        }
    }
}
