package com.malinskiy.marathon.android

import com.fasterxml.jackson.annotation.JsonProperty
import com.malinskiy.marathon.device.DeviceFeature
import java.util.concurrent.TimeUnit

data class ScreenRecordConfiguration(
    @JsonProperty("preferableRecorderType") val preferableRecorderType: DeviceFeature? = null,
    @JsonProperty("videoConfiguration") val videoConfiguration: VideoConfiguration = VideoConfiguration(),
    @JsonProperty("screenshotConfiguration") val screenshotConfiguration: ScreenshotConfiguration = ScreenshotConfiguration()
)

data class ScreenshotConfiguration(
    @JsonProperty("enabled") val enabled: Boolean = true,
    @JsonProperty("width") val width: Int = 720,
    @JsonProperty("height") val height: Int = 1280,
    @JsonProperty("delayMs") val delayMs: Int = 500
)

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
) {
    fun toScreenRecorderCommand(remoteFilePath: String): String {
        val sb = StringBuilder()

        sb.append("screenrecord")
        sb.append(' ')

        if (width > 0 && height > 0) {
            sb.append("--size ")
            sb.append(width)
            sb.append('x')
            sb.append(height)
            sb.append(' ')
        }

        if (bitrateMbps > 0) {
            sb.append("--bit-rate ")
            var bitrate = bitrateMbps * 1_000_000
            /**
             * screenrecord supports bitrate up to 200Mbps
             */
            if (bitrate > 200_000_000) {
                bitrate = 200_000_000
            }
            sb.append(bitrate)
            sb.append(' ')
        }

        if (timeLimit > 0) {
            sb.append("--time-limit ")
            var seconds = TimeUnit.SECONDS.convert(timeLimit, timeLimitUnits)
            if (seconds > 180) {
                seconds = 180
            }
            sb.append(seconds)
            sb.append(' ')
        }

        sb.append(remoteFilePath)

        return sb.toString()
    }
}
