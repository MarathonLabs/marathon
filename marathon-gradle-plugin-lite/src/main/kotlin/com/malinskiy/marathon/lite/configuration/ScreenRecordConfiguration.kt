package com.malinskiy.marathon.lite.configuration


import com.google.protobuf.Duration
import com.malinskiy.marathon.cliconfig.proto.AndroidConfiguration
import java.io.Serializable
import java.util.concurrent.TimeUnit
import com.malinskiy.marathon.cliconfig.proto.AndroidConfiguration.ScreenRecordConfiguration.ScreenshotConfiguration as ProtoScreenshotConfiguration
import com.malinskiy.marathon.cliconfig.proto.AndroidConfiguration.ScreenRecordConfiguration.VideoConfiguration as ProtoVideoConfiguration

data class ScreenRecordConfiguration(
    val preferableRecorderType: DeviceFeature? = null,
    val videoConfiguration: VideoConfiguration? = null,
    val screenshotConfiguration: ScreenshotConfiguration? = null
) : Serializable

data class ScreenshotConfiguration(
    val enabled: Boolean = true,
    val width: Int = 720,
    val height: Int = 1280,
    val delayMs: Int = 500
) : Serializable

data class VideoConfiguration(
    val enabled: Boolean = true,
    val width: Int = 720,
    val height: Int = 1280,
    val bitrateMbps: Int = 1,
    val timeLimit: Long = 180,
    val timeLimitUnits: TimeUnit = TimeUnit.SECONDS
) : Serializable

fun ScreenRecordConfiguration.toProto(): AndroidConfiguration.ScreenRecordConfiguration {
    val builder = AndroidConfiguration.ScreenRecordConfiguration.newBuilder()
    preferableRecorderType?.let { builder.setPreferableRecorderType(it.toProto()) }
    videoConfiguration?.let { builder.setVideoConfiguration(it.toProto()) }
    screenshotConfiguration?.let { builder.setScreenshotConfiguration(it.toProto()) }
    return builder.build()
}

private fun VideoConfiguration.toProto(): ProtoVideoConfiguration {
    val builder = ProtoVideoConfiguration.newBuilder()
    builder.enabled = enabled
    builder.width = width
    builder.height = height
    builder.bitmapMbps = bitrateMbps
    builder.duration = Duration.newBuilder().setSeconds(timeLimitUnits.toSeconds(timeLimit)).build()
    return builder.build()
}

private fun ScreenshotConfiguration.toProto(): ProtoScreenshotConfiguration {
    val builder = ProtoScreenshotConfiguration.newBuilder()
    builder.enabled = enabled
    builder.width = width
    builder.height = height
    builder.delayMs = delayMs
    return builder.build()
}
