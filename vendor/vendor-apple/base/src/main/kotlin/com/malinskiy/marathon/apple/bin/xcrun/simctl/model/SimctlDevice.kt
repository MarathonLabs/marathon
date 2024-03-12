package com.malinskiy.marathon.apple.bin.xcrun.simctl.model

import com.google.gson.annotations.SerializedName

/**
 * @param deviceTypeIdentifier null if device type is missing due to Xcode migrations
 */
data class SimctlDevice(
    @SerializedName("runtime") val runtime: String?,
    @SerializedName("state") val state: State,
    @SerializedName("name") val name: String,
    @SerializedName("udid") val udid: String,
    @SerializedName("logPath") val logPath: String?,
    @SerializedName("isAvailable") val isAvailable: Boolean?,
    @SerializedName("availabilityError") val availabilityError: String?,
    @SerializedName("dataPath") val dataPath: String?,
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
