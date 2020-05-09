package com.malinskiy.marathon.android.executor.listeners.video

import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.log.MarathonLogging
import kotlinx.coroutines.CancellationException
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.system.measureTimeMillis

internal class ScreenRecorder(
    private val device: AndroidDevice,
    private val remoteFilePath: String
) {

    suspend fun run() {
        try {
            startRecordingTestVideo()
        } catch (e: CancellationException) {
            //Ignore
        } catch (e: Exception) {
            logger.error("Something went wrong while screen recording", e)
        }
    }

    private suspend fun startRecordingTestVideo() {
        val millis = measureTimeMillis {
            device.safeStartScreenRecorder(
                remoteFilePath = remoteFilePath,
                options = options
            )
        }
        logger.debug { "Recording finished in ${millis}ms $remoteFilePath" }
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
