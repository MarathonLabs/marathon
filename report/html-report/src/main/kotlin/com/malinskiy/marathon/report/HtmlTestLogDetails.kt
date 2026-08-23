package com.malinskiy.marathon.report

import com.google.gson.annotations.SerializedName

/**
 * Log page payload. Holds one entry per attempt so the viewer can offer an
 * attempt selector and honor a `#/attempt/N` URL hash on load.
 */
data class HtmlTestLogDetails(
    @SerializedName("pool_id") val poolId: String,
    @SerializedName("test_id") val testId: String,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("attempts") val attempts: List<HtmlLogAttempt>,
)

data class HtmlLogAttempt(
    @SerializedName("attempt_index") val attemptIndex: Int,
    @SerializedName("final") val final: Boolean,
    @SerializedName("status") val status: Status,
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("device") val device: HtmlDevice,
    @SerializedName("log_path") val logPath: String,
    /**
     * Log text inlined at emit time. Preferred over a runtime `fetch(logPath)`
     * because Chromium blocks `fetch()` under `file://` (a marathon report is
     * typically opened by double-clicking `index.html`). Null when the log
     * exceeded the [HtmlSummaryReporter.LOG_INLINE_CAP_BYTES] size cap, in
     * which case the viewer falls back to the `logPath` fetch (still useful in
     * Firefox / dev servers).
     */
    @SerializedName("log_body") val logBody: String?,
)
