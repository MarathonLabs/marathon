package com.malinskiy.marathon.config.vendor.android

import com.fasterxml.jackson.annotation.JsonProperty

data class ScreenshotConfiguration(
    @JsonProperty("enabled") val enabled: Boolean = true,
    @JsonProperty("width") val width: Int = 720,
    @JsonProperty("height") val height: Int = 1280,
    @JsonProperty("delayMs") val delayMs: Int = 500
)
