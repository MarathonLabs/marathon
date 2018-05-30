package com.malinskiy.marathon.report

import com.google.gson.annotations.SerializedName

data class HtmlIndex(
        @SerializedName("title") val title: String,
        @SerializedName("total_failed") val totalFailed: Int,
        @SerializedName("total_flaky") val totalFlaky: Int,
        @SerializedName("total_ignored") val totalIgnored: Int,
        @SerializedName("total_passed") val totalPassed: Int,
        @SerializedName("total_duration_millis") val totalDuration: Long,
        @SerializedName("average_duration_millis") val averageDuration: Long,
        @SerializedName("max_duration_millis") val maxDuration: Long,
        @SerializedName("min_duration_millis") val minDuration: Long,
        @SerializedName("pools") val pools: List<HtmlPoolSummary>)
