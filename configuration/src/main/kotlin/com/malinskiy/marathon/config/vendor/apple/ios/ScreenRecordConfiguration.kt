package com.malinskiy.marathon.config.vendor.apple.ios

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Locale

data class ScreenRecordConfiguration(
    @JsonProperty("preferableRecorderType") val preferableRecorderType: RecorderType? = null,
    @JsonProperty("videoConfiguration") val videoConfiguration: VideoConfiguration = VideoConfiguration(),
    @JsonProperty("screenshotConfiguration") val screenshotConfiguration: ScreenshotConfiguration = ScreenshotConfiguration()
)

enum class RecorderType {
    VIDEO,
    SCREENSHOT;

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromString(key: String?): RecorderType? {
            return key?.let {
                RecorderType.valueOf(it.uppercase(Locale.ENGLISH))
            }
        }
    }
}
