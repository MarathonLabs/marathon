package com.malinskiy.marathon.apple.model

enum class Sdk(val value: String) {
    IPHONEOS("iphoneos"),
    IPHONESIMULATOR("iphonesimulator"),
    TV("appletvos"),
    TV_SIMULATOR("appletvsimulator"),
    WATCH("watchos"),
    WATCH_SIMULATOR("watchsimulator"),
    VISION("xros"),
    VISION_SIMULATOR("xrsimulator"),
    MACOS("macosx");
    
    val platformName: String by lazy { 
        when(this) {
            IPHONEOS -> "iPhoneOS"
            IPHONESIMULATOR -> "iPhoneSimulator"
            MACOS -> "MacOSX"
            TV -> "AppleTVOS"
            TV_SIMULATOR -> "AppleTVSimulator"
            WATCH -> "WatchOS"
            WATCH_SIMULATOR -> "WatchSimulator"
            VISION -> "XROS"
            VISION_SIMULATOR -> "XRSimulator"
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
            TV -> "tvOS"
            TV_SIMULATOR -> "tvOS Simulator"
            WATCH -> "watchOS"
            WATCH_SIMULATOR -> "watchOS Simulator"
            VISION -> "visionOS"
            VISION_SIMULATOR -> "visionOS Simulator"
        }
    }
}
