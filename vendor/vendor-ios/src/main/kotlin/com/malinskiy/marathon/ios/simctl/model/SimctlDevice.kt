package com.malinskiy.marathon.ios.simctl.model

import com.google.gson.annotations.SerializedName

data class SimctlDevice(
    @SerializedName("runtime") val runtime: String?,
    @SerializedName("state") val state: State,
    @SerializedName("name") val name: String,
    @SerializedName("udid") val udid: String,
    @SerializedName("logPath") val logPath: String?,
    @SerializedName("isAvailable") val isAvailable: Boolean?,
    @SerializedName("deviceTypeIdentifier") val deviceTypeIdentifier: String?,
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
