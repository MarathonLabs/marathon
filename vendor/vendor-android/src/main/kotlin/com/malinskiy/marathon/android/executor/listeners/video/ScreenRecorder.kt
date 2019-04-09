package com.malinskiy.marathon.android.executor.listeners.video

import com.android.ddmlib.CollectingOutputReceiver
import com.android.ddmlib.IDevice
import com.android.ddmlib.ScreenRecorderOptions
import com.malinskiy.marathon.android.safeStartScreenRecorder
import com.malinskiy.marathon.log.MarathonLogging
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.system.measureTimeMillis

internal class ScreenRecorder(private val device: IDevice,
                              private val receiver : CollectingOutputReceiver,
                              private val remoteFilePath : String) : Runnable {

    override fun run() {
        try {
            startRecordingTestVideo()
        } catch (e: Exception) {
            logger.error("Something went wrong while screen recording", e)
        }
    }

    private fun startRecordingTestVideo() {
        val millis = measureTimeMillis {
            device.safeStartScreenRecorder(remoteFilePath, options, receiver)
        }
        logger.trace { "Recording finished in ${millis}ms $remoteFilePath" }
    }

    companion object {
        private val logger = MarathonLogging.logger("ScreenRecorder")
        private const val DURATION = 60
        private const val BITRATE_MB_PER_SECOND = 1
        private val options = ScreenRecorderOptions.Builder()
                .setTimeLimit(DURATION.toLong(), SECONDS)
                .setBitRate(BITRATE_MB_PER_SECOND)
                .build()
    }
}
