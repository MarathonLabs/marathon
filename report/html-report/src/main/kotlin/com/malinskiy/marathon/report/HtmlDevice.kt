package com.malinskiy.marathon.report

import com.google.gson.annotations.SerializedName

data class HtmlDevice(
    @SerializedName("apiLevel") val apiLevel: String,
    @SerializedName("isTable") val isTablet: Boolean,
    @SerializedName("serial") val serial: String,
    @SerializedName("modelName") val modelName: String
)
