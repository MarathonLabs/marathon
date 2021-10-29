package com.malinskiy.marathon.config.vendor.android

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.concurrent.TimeUnit

/**
 * See https://android.googlesource.com/platform/frameworks/av/+/master/cmds/screenrecord/screenrecord.cpp for a list of latest defaults
 */
data class VideoConfiguration(
    @JsonProperty("enabled") val enabled: Boolean = true,
    @JsonProperty("width") val width: Int = 720,
    @JsonProperty("height") val height: Int = 1280,
    @JsonProperty("bitrateMbps") val bitrateMbps: Int = 1,
    @JsonProperty("timeLimit") val timeLimit: Long = 180,
    @JsonProperty("timeLimitUnits") val timeLimitUnits: TimeUnit = TimeUnit.SECONDS
)
