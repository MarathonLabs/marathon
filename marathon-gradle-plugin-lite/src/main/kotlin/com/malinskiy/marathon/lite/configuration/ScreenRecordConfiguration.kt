package com.malinskiy.marathon.lite.configuration


import com.malinskiy.marathon.cliconfig.proto.AndroidConfiguration
import java.io.Serializable
import java.util.concurrent.TimeUnit

data class ScreenRecordConfiguration(
    val preferableRecorderType: DeviceFeature,
    val videoConfiguration: VideoConfiguration?,
    val screenshotConfiguration: ScreenshotConfiguration?
) : Serializable

data class ScreenshotConfiguration(
    val enabled: Boolean,
    val width: Int,
    val height: Int,
    val delayMs: Int
) : Serializable

data class VideoConfiguration(
    val enabled: Boolean,
    val width: Int,
    val height: Int,
    val bitrateMbps: Int,
    val timeLimit: Long,
    val timeLimitUnits: TimeUnit
) : Serializable

fun ScreenRecordConfiguration.toProto(): AndroidConfiguration.ScreenRecordConfiguration {
    val builder = AndroidConfiguration.ScreenRecordConfiguration.newBuilder()
    builder.preferableRecorderType = preferableRecorderType.toProto()
//        .setScreenshotConfiguration()
//        .setVideoConfiguration()

    return builder.build()
}
