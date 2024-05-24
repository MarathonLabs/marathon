package com.malinskiy.marathon.android.executor.listeners.video

import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.config.vendor.android.VideoConfiguration
import com.malinskiy.marathon.log.MarathonLogging
import kotlinx.coroutines.CancellationException
import kotlin.system.measureTimeMillis

internal class ScreenRecorder(
    private val device: AndroidDevice,
    private val videoConfiguration: VideoConfiguration,
    private val remoteFilePath: String
) {

    suspend fun run() {
        try {
            startRecordingTestVideo()
        } catch (e: CancellationException) {
            logger.warn(e) { "screenrecord start was interrupted" }
        } catch (e: Exception) {
            logger.error("Something went wrong while screen recording", e)
        }
    }

    private suspend fun startRecordingTestVideo() {
        val millis = measureTimeMillis {
            device.safeStartScreenRecorder(
                remoteFilePath = remoteFilePath,
                options = videoConfiguration
            )
        }
        logger.debug { "Recording finished in ${millis}ms $remoteFilePath" }
    }

    companion object {
        private val logger = MarathonLogging.logger("ScreenRecorder")
        private const val VIDEO_SUFFIX_LENGTH = 41 // "-" + 36 chars UUID of testBatchId + ".mp4"
        fun String.addFileNumberForVideo(fileNumber: String): String  {
            val lastIndexForReplacement = this.length - VIDEO_SUFFIX_LENGTH
            var firstIndexForReplacement = lastIndexForReplacement
            if (this.length + fileNumber.length > 255) {
                firstIndexForReplacement -= fileNumber.length
            }
            return this.replaceRange(firstIndexForReplacement, lastIndexForReplacement, fileNumber)
        }
    }
}
