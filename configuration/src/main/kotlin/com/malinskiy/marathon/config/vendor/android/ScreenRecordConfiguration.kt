package com.malinskiy.marathon.config.vendor.android

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class ScreenRecordConfiguration(
    @JsonProperty("preferableRecorderType") val preferableRecorderType: RecorderType? = null,
    @JsonProperty("videoConfiguration") val videoConfiguration: VideoConfiguration = VideoConfiguration(),
    @JsonProperty("screenshotConfiguration") val screenshotConfiguration: ScreenshotConfiguration = ScreenshotConfiguration()
) : Serializable

enum class RecorderType {
    VIDEO,
    SCREENSHOT;

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromString(key: String?): RecorderType? {
            return key?.let {
                RecorderType.valueOf(it.toUpperCase())
            }
        }
    }
}
