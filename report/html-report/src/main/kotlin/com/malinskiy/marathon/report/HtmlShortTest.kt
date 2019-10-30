package com.malinskiy.marathon.report

import com.google.gson.annotations.SerializedName

data class HtmlShortTest(
    @SerializedName("id") val id: String,
    @SerializedName("package_name") val packageName: String,
    @SerializedName("class_name") val className: String,
    @SerializedName("name") val name: String,
    @SerializedName("duration_millis") val durationMillis: Long,
    @SerializedName("status") val status: Status,
    @SerializedName("deviceId") val deviceId: String
)
