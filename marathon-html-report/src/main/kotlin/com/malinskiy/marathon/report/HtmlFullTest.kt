package com.malinskiy.marathon.report

import com.google.gson.annotations.SerializedName

data class HtmlFullTest(
        @SerializedName("pool_id") val poolId: String,
        @SerializedName("package_name") val packageName: String,
        @SerializedName("class_name") val className: String,
        @SerializedName("name") val name: String,
        @SerializedName("id") val id: String = "$packageName$className$name",
        @SerializedName("duration_millis") val durationMillis: Long,
        @SerializedName("status") val status: Status,
        @SerializedName("stacktrace") val stacktrace: String?,
        @SerializedName("deviceId") val deviceId: String,
        @SerializedName("diagnostic_video") val diagnosticVideo: Boolean,
        @SerializedName("diagnostic_screenshots") val diagnosticScreenshots: Boolean,
        @SerializedName("screenshot") val screenshot: String,
        @SerializedName("video") val video: String,
        @SerializedName("log_file") val logFile : String
)
