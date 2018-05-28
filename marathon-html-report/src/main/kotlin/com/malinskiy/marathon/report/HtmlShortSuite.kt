package com.malinskiy.marathon.report

import com.google.gson.annotations.SerializedName

data class HtmlShortSuite(@SerializedName("id") val id: String,
                          @SerializedName("passed_count") val passedCount: Int,
                          @SerializedName("ignored_count") val ignoredCount: Int,
                          @SerializedName("failed_count") val failedCount: Int,
                          @SerializedName("duration_millis") val durationMillis: Long,
                          @SerializedName("devices") val devices: List<HtmlDevice>)