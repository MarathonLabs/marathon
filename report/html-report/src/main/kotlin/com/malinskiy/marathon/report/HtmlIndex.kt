package com.malinskiy.marathon.report

import com.google.gson.annotations.SerializedName

data class HtmlIndex(
    @SerializedName("report_schema_version") val reportSchemaVersion: Int = REPORT_SCHEMA_VERSION,
    @SerializedName("generated_at_ms") val generatedAtMs: Long,
    @SerializedName("title") val title: String,
    @SerializedName("total_failed") val totalFailed: Int,
    @SerializedName("total_flaky") val totalFlaky: Int,
    @SerializedName("total_ignored") val totalIgnored: Int,
    @SerializedName("total_passed") val totalPassed: Int,
    @SerializedName("total_duration_millis") val totalDuration: Long,
    @SerializedName("average_duration_millis") val averageDuration: Long,
    @SerializedName("max_duration_millis") val maxDuration: Long,
    @SerializedName("min_duration_millis") val minDuration: Long,
    @SerializedName("pools") val pools: List<HtmlPoolSummary>,
) {
    companion object {
        /**
         * Bump when JSON payload shape changes in a way the UI must key on.
         * V1: legacy, camelCase device fields, no attempts, no timestamps.
         * V2: snake_case everywhere, per-attempt history, device details, timestamps.
         */
        const val REPORT_SCHEMA_VERSION = 2
    }
}
