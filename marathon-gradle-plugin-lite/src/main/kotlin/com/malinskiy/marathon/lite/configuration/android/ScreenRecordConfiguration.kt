package com.malinskiy.marathon.lite.configuration.android

import com.malinskiy.marathon.lite.configuration.common.DeviceFeature
import java.io.Serializable
import java.util.concurrent.TimeUnit

data class ScreenRecordConfiguration(
    val preferableRecorderType: DeviceFeature? = null,
    val videoConfiguration: VideoConfiguration = VideoConfiguration(),
    val screenshotConfiguration: ScreenshotConfiguration = ScreenshotConfiguration()
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
