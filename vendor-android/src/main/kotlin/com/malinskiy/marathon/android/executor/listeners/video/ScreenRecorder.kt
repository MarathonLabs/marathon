package com.malinskiy.marathon.android.executor.listeners.video

import com.android.ddmlib.IDevice
import com.android.ddmlib.NullOutputReceiver
import com.android.ddmlib.ScreenRecorderOptions
import com.android.ddmlib.testrunner.TestIdentifier
import com.malinskiy.marathon.android.RemoteFileManager
import com.malinskiy.marathon.android.RemoteFileManager.removeRemotePath
import mu.KotlinLogging
import java.io.File
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.system.measureTimeMillis

internal class ScreenRecorder(private val deviceInterface: IDevice,
                              private val localVideoFile: File,
                              test: TestIdentifier,
                              private val screenRecorderStopper: ScreenRecorderStopper) : Runnable {

    private val remoteFilePath: String = RemoteFileManager.remoteVideoForTest(test)

    override fun run() {
        try {
            startRecordingTestVideo()
            if (screenRecorderStopper.hasFailed()) {
                pullTestVideo()
            }
            removeTestVideo()
        } catch (e: Exception) {
            logger.error("Something went wrong while screen recording", e)
        }
    }

    private fun startRecordingTestVideo() {
        val outputReceiver = NullOutputReceiver()
        val millis = measureTimeMillis {
            deviceInterface.startScreenRecorder(remoteFilePath, options, outputReceiver)
        }
        logger.trace { "Recording finished in ${millis}ms $remoteFilePath" }
    }

    private fun pullTestVideo() {
        val millis = measureTimeMillis {
            deviceInterface.pullFile(remoteFilePath, localVideoFile.toString())
        }
        logger.trace { "Pulling finished in ${millis}ms $remoteFilePath " }
    }

    private fun removeTestVideo() {
        val millis = measureTimeMillis {
            removeRemotePath(deviceInterface, remoteFilePath)
        }
        logger.trace("Removed file in ${millis}ms $remoteFilePath")
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
