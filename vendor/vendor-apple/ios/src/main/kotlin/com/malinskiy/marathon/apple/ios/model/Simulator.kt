package com.malinskiy.marathon.apple.ios.model

class Simulator {
    data class Profile(
        val sdk: Sdk,
        val os: OS
    )
    
    enum class State {
        CREATING,
        SHUTDOWN,
        BOOTING,
        BOOTED,
        UNKNOWN;
    }
}

