package com.malinskiy.marathon.android.executor.listeners.video

import java.util.concurrent.TimeUnit

data class ScreenRecorderOptions(
    val width: Int,
    val height: Int,
    val bitrateMbps: Int,
    val timeLimit: Long,
    val timeLimitUnits: TimeUnit,
    val showTouches: Boolean
)