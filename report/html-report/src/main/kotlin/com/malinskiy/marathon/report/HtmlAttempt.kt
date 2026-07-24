package com.malinskiy.marathon.report

import com.google.gson.annotations.SerializedName

/**
 * One execution attempt of a test. A single logical test may have multiple
 * attempts across retries; each attempt can run on a different device
 * (same pool). [final] marks the attempt whose outcome is reflected in
 * the pool summary.
 */
data class HtmlAttempt(
    @SerializedName("attempt_index") val attemptIndex: Int,
    @SerializedName("final") val final: Boolean,
    @SerializedName("status") val status: Status,
    @SerializedName("start_time_ms") val startTimeMs: Long,
    @SerializedName("end_time_ms") val endTimeMs: Long,
    @SerializedName("duration_millis") val durationMillis: Long,
    @SerializedName("batch_id") val batchId: String,
    @SerializedName("device") val device: HtmlDevice,
    @SerializedName("stacktrace") val stacktrace: String?,
    @SerializedName("screenshot") val screenshot: String,
    @SerializedName("videos") val videos: List<String>,
    @SerializedName("log_file") val logFile: String,
)
