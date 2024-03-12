package com.malinskiy.marathon.apple.ios.model

import com.malinskiy.marathon.apple.model.OS
import com.malinskiy.marathon.apple.model.Sdk

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

