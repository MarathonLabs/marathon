package com.malinskiy.marathon.config.vendor.apple.ios

import com.fasterxml.jackson.annotation.JsonProperty

data class XcresultConfiguration(
    @JsonProperty("pull") val pull: Boolean = true,
    @JsonProperty("remoteClean") val remoteClean: Boolean = true,
    @JsonProperty("attachments") val attachments: AttachmentsConfiguration = AttachmentsConfiguration(),
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
