package com.malinskiy.marathon.report

import com.google.gson.annotations.SerializedName

/**
 * Row-sized projection of a test for pool list rendering. Includes the final
 * attempt's device plus multi-attempt hints ([devices], [osVersions]) so the
 * pool page can filter across every device a test touched.
 */
data class HtmlShortTest(
    @SerializedName("id") val id: String,
    @SerializedName("package_name") val packageName: String,
    @SerializedName("filename") val fileName: String,
    @SerializedName("class_name") val className: String,
    @SerializedName("name") val name: String,
    @SerializedName("duration_millis") val durationMillis: Long,
    @SerializedName("status") val status: Status,
    @SerializedName("start_time_ms") val startTimeMs: Long,
    @SerializedName("end_time_ms") val endTimeMs: Long,
    @SerializedName("batch_id") val batchId: String,
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("device") val device: HtmlDevice,
    @SerializedName("attempt_count") val attemptCount: Int,
    @SerializedName("is_flaky") val isFlaky: Boolean,
    /** Distinct device serials the test ran on across all attempts. */
    @SerializedName("devices") val devices: List<String>,
    /** Distinct OS versions the test ran on across all attempts. */
    @SerializedName("os_versions") val osVersions: List<String>,
    /**
     * True when any attempt of this test produced a captured screenshot the
     * report actually links to. Populated by the Kotlin reporter from the
     * same `artifactScreenshotPath` result the test detail page uses, so
     * the pool page's "screenshot" filter reflects real assets rather than
     * device capability.
     */
    @SerializedName("has_screenshot") val hasScreenshot: Boolean,
    /** Same shape as [hasScreenshot], for captured video attachments. */
    @SerializedName("has_video") val hasVideo: Boolean,
)
