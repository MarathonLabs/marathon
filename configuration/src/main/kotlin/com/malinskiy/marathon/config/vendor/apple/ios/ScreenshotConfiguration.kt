package com.malinskiy.marathon.config.vendor.apple.ios

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Duration

/**
 * screenshot [--type=<type>] [--display=<display>] [--mask=<policy>] <file or url>
 *             Saves a screenshot as a PNG to the specified file or url(use "-" for stdout).
 *             --type       Can be "png", "tiff", "bmp", "gif", "jpeg". Default is png.
 *
 *             --display    iOS: supports "internal" or "external". Default is "internal".
 *                          tvOS: supports only "external"
 *                          watchOS: supports only "internal"
 *
 *                          You may also specify a port by UUID
 *             --mask       For non-rectangular displays, handle the mask by policy:
 *                          ignored: The mask is ignored and the unmasked framebuffer is saved.
 *                          alpha: The mask is used as premultiplied alpha.
 *                          black: The mask is rendered black.
 *                          
 * Notes: GIF doesn't really work and produces `Invalid file type: gif` with latest xcode                         
 */
data class ScreenshotConfiguration(
    @JsonProperty("enabled") val enabled: Boolean = true,
    @JsonProperty("type") val type: Type = Type.JPEG,
    @JsonProperty("display") val display: Display = Display.INTERNAL,
    @JsonProperty("mask") val mask: Mask = Mask.BLACK,
    @JsonProperty("delay") val delay: Duration = Duration.ofMillis(500),
    @JsonProperty("width") val width: Int = 720,
    @JsonProperty("height") val height: Int = 1280,
)

enum class Type(val value: String) {
    @JsonProperty("png") PNG("png"),
    @JsonProperty("tiff") TIFF(("tiff")),
    @JsonProperty("bmp") BMP("bmp"),
    @JsonProperty("jpeg") JPEG("jpeg"),
}
