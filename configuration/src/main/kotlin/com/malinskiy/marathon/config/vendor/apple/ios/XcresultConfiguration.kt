package com.malinskiy.marathon.config.vendor.apple.ios

import com.fasterxml.jackson.annotation.JsonProperty

data class XcresultConfiguration(
    @JsonProperty("pull") val pull: Boolean? = null,
    @JsonProperty("pullingPolicy") val pullingPolicy: PullingPolicy = PullingPolicy.ALWAYS,
    @JsonProperty("remoteClean") val remoteClean: Boolean = true,
    @JsonProperty("attachments") val attachments: AttachmentsConfiguration = AttachmentsConfiguration(),
    @JsonProperty("preferredScreenCaptureFormat") val preferredScreenCaptureFormat: ScreenCaptureFormat? = null,
)

data class AttachmentsConfiguration(
    @JsonProperty("systemAttachmentLifetime") val systemAttachmentLifetime: Lifetime = Lifetime.DELETE_ON_SUCCESS,
    @JsonProperty("userAttachmentLifetime") val userAttachmentLifetime: Lifetime = Lifetime.KEEP_ALWAYS,
)

enum class Lifetime(val value: String) {
    /**
     * Always keep attachments, even for tests that succeed.
     */
    @JsonProperty("KEEP_ALWAYS") KEEP_ALWAYS("keepAlways"),

    /**
     * Keep attachments for tests that fail, and discard them for tests that succeed.
     */
    @JsonProperty("DELETE_ON_SUCCESS") DELETE_ON_SUCCESS("deleteOnSuccess"),

    /**
     * Always discard attachments, regardless of whether the test succeeds or fails.
     */
    @JsonProperty("KEEP_NEVER") KEEP_NEVER("keepNever");
}

enum class PullingPolicy {
    @JsonProperty("NEVER") NEVER,
    @JsonProperty("ALWAYS") ALWAYS,
    @JsonProperty("ON_FAILURE") ON_FAILURE,
}

enum class ScreenCaptureFormat {
    @JsonProperty("SCREENSHOTS") SCREENSHOTS,
    @JsonProperty("SCREEN_RECORDING") SCREEN_RECORDING;

    fun xcodevalue() = when(this) {
        SCREENSHOTS -> "screenshots"
        SCREEN_RECORDING -> "screenRecording"
    }
}
