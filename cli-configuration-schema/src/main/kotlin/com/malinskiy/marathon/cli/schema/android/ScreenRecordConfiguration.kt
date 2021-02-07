package com.malinskiy.marathon.cli.schema.android

import com.malinskiy.marathon.device.DeviceFeature
import java.util.concurrent.TimeUnit

data class ScreenRecordConfiguration(
    val preferableRecorderType: DeviceFeature? = null,
    val videoConfiguration: VideoConfiguration = VideoConfiguration(),
    val screenshotConfiguration: ScreenshotConfiguration = ScreenshotConfiguration()
)

data class ScreenshotConfiguration(
    val enabled: Boolean = true,
    val width: Int = 720,
    val height: Int = 1280,
    val delayMs: Int = 500
)

data class VideoConfiguration(
    val enabled: Boolean = true,
    val width: Int = 720,
    val height: Int = 1280,
    val bitrateMbps: Int = 1,
    val timeLimit: Long = 180,
    val timeLimitUnits: TimeUnit = TimeUnit.SECONDS
)
