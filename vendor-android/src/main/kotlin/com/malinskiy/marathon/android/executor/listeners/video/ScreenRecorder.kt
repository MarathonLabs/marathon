package com.malinskiy.marathon.android.executor.listeners.video

import com.android.ddmlib.CollectingOutputReceiver
import com.android.ddmlib.IDevice
import com.android.ddmlib.ScreenRecorderOptions
import com.android.ddmlib.testrunner.TestIdentifier
import com.malinskiy.marathon.android.RemoteFileManager
import mu.KotlinLogging
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.system.measureTimeMillis

internal class ScreenRecorder(private val device: IDevice,
                              test: TestIdentifier,
                              private val receiver : CollectingOutputReceiver) : Runnable {

    private val remoteFilePath: String = RemoteFileManager.remoteVideoForTest(test)

    override fun run() {
        try {
            startRecordingTestVideo()
        } catch (e: Exception) {
            logger.error("Something went wrong while screen recording", e)
        }
    }

    private fun startRecordingTestVideo() {
        val millis = measureTimeMillis {
            device.startScreenRecorder(remoteFilePath, options, receiver)
        }
        logger.trace { "Recording finished in ${millis}ms $remoteFilePath" }
    }

    companion object {
        private val logger = KotlinLogging.logger("ScreenRecorder")
        private const val DURATION = 60
        private const val BITRATE_MB_PER_SECOND = 1
        private val options = ScreenRecorderOptions.Builder()
                .setTimeLimit(DURATION.toLong(), SECONDS)
                .setBitRate(BITRATE_MB_PER_SECOND)
                .build()
    }
}
