package com.malinskiy.marathon.ios.simctl.model

import com.google.gson.annotations.SerializedName

data class SimctlDevice(
    val runtime: String,
    val state: State,
    val name: String,
    val udid: String
) {
    enum class State {
        Unknown,
        @SerializedName("Booted")
        Booted,
        @SerializedName("Shutdown")
        Shutdown,
        @SerializedName("Creating")
        Creating,
        @SerializedName("Booting")
        Booting,
        @SerializedName("Shutting Down")
        ShuttingDown
    }
}
