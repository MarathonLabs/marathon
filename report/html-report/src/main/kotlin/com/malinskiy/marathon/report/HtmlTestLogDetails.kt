package com.malinskiy.marathon.report

import com.google.gson.annotations.SerializedName

data class HtmlTestLogDetails(
    @SerializedName("pool_id") val poolId: String,
    @SerializedName("test_id") val testId: String,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("log_path") val logPath: String
)
