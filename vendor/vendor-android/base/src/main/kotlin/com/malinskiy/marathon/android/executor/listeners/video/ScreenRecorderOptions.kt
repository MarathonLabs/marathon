package com.malinskiy.marathon.android.executor.listeners.video

import java.util.concurrent.TimeUnit

data class ScreenRecorderOptions(
    val width: Int,
    val height: Int,
    val bitrateMbps: Int,
    val timeLimit: Long,
    val timeLimitUnits: TimeUnit,
    val showTouches: Boolean
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
            sb.append(bitrateMbps * 1_000_000)
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
