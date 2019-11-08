package com.malinskiy.marathon.android.executor.listeners.video

import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.executor.listeners.line.LineListener
import com.malinskiy.marathon.log.MarathonLogging
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.system.measureTimeMillis

internal class ScreenRecorder(
    private val device: AndroidDevice,
    private val receiver: LineListener,
    private val remoteFilePath: String
) : Runnable {

    override fun run() {
        try {
            startRecordingTestVideo()
        } catch (e: Exception) {
            logger.error("Something went wrong while screen recording", e)
        }
    }

    private fun startRecordingTestVideo() {
        val millis = measureTimeMillis {
            device.safeStartScreenRecorder(
                remoteFilePath = remoteFilePath,
                listener = receiver,
                options = options
            )
        }
        logger.trace { "Recording finished in ${millis}ms $remoteFilePath" }
    }

    companion object {
        private val logger = MarathonLogging.logger("ScreenRecorder")
        private const val DURATION = 180
        private const val BITRATE_MB_PER_SECOND = 1
        private val options = ScreenRecorderOptions(
            0,
            0,
            BITRATE_MB_PER_SECOND,
            timeLimit = DURATION.toLong(),
            timeLimitUnits = SECONDS,
            showTouches = false
        )
    }
}
