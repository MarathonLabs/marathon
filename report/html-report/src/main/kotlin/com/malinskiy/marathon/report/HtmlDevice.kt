package com.malinskiy.marathon.report

import com.google.gson.annotations.SerializedName

data class HtmlDevice(
    @SerializedName("serial") val serial: String,
    @SerializedName("model_name") val modelName: String,
    @SerializedName("manufacturer") val manufacturer: String,
    @SerializedName("os_version") val osVersion: String,
    @SerializedName("os_major") val osMajor: Int?,
    @SerializedName("network_state") val networkState: String,
    @SerializedName("features") val features: List<String>,
    @SerializedName("is_tablet") val isTablet: Boolean,
    @SerializedName("api_level") val apiLevel: String,
)
