package com.malinskiy.marathon.config.vendor.ios

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * recordVideo [--codec=<codec>] [--display=<display>] [--mask=<policy>] [--force] <file or url>
 *             Records the display to a QuickTime movie at the specified file or url.
 *             --codec      Specifies the codec type: "h264" or "hevc". Default is "hevc".
 *
 *             --display    iOS: supports "internal" or "external". Default is "internal".
 *                          tvOS: supports only "external"
 *                          watchOS: supports only "internal"
 *
 *             --mask       For non-rectangular displays, handle the mask by policy:
 *                          ignored: The mask is ignored and the unmasked framebuffer is saved.
 *                          alpha: Not supported, but retained for compatibility; the mask is rendered black.
 *                          black: The mask is rendered black.
 *
 *             --force      Force the output file to be written to, even if the file already exists.
 */
data class VideoConfiguration(
    @JsonProperty("enabled") val enabled: Boolean = true,
    @JsonProperty("codec") val codec: Codec = Codec.H264,
    @JsonProperty("display") val display: Display = Display.INTERNAL,
    @JsonProperty("mask") val mask: Mask = Mask.BLACK,
)

enum class Codec(val value: String) {
    @JsonProperty("h264") H264("h264"),

    /**
     * Might not work everywhere playback-wise since support for h265 is not universal yet
     */
    @JsonProperty("hevc") HEVC("hevc"),
}

