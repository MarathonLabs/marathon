package com.malinskiy.marathon.report

import com.google.gson.annotations.SerializedName

/**
 * Full detail for a single logical test. Top-level artifact/status fields
 * mirror the final attempt for cheap first-paint; [attempts] holds the
 * complete history in chronological order.
 */
data class HtmlFullTest(
    @SerializedName("pool_id") val poolId: String,
    @SerializedName("package_name") val packageName: String,
    @SerializedName("class_name") val className: String,
    @SerializedName("name") val name: String,
    @SerializedName("id") val id: String = "$packageName.$className.$name",
    @SerializedName("filename") val filename: String,
    @SerializedName("duration_millis") val durationMillis: Long,
    @SerializedName("status") val status: Status,
    @SerializedName("stacktrace") val stacktrace: String?,
    @SerializedName("start_time_ms") val startTimeMs: Long,
    @SerializedName("end_time_ms") val endTimeMs: Long,
    @SerializedName("batch_id") val batchId: String,
    @SerializedName("device") val device: HtmlDevice,
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("diagnostic_video") val diagnosticVideo: Boolean,
    @SerializedName("diagnostic_screenshots") val diagnosticScreenshots: Boolean,
    @SerializedName("screenshot") val screenshot: String,
    @SerializedName("videos") val videos: List<String>,
    @SerializedName("log_file") val logFile: String,
    @SerializedName("attempts") val attempts: List<HtmlAttempt>,
    @SerializedName("attempt_count") val attemptCount: Int,
    @SerializedName("is_flaky") val isFlaky: Boolean,
    @SerializedName("distinct_devices") val distinctDevices: List<HtmlDevice>,
)
