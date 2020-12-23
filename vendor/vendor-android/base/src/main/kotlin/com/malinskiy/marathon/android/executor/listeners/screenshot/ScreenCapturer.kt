package com.malinskiy.marathon.android.executor.listeners.screenshot

import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.ScreenshotConfiguration
import com.malinskiy.marathon.android.exception.CommandRejectedException
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FileType
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.imgscalr.Scalr
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.awt.image.RenderedImage
import java.io.IOException
import java.time.Duration
import java.util.concurrent.TimeoutException
import javax.imageio.stream.FileImageOutputStream
import kotlin.system.measureTimeMillis


class ScreenCapturer(
    val device: AndroidDevice,
    private val poolId: DevicePoolId,
    private val fileManager: FileManager,
    val test: Test,
    private val configuration: ScreenshotConfiguration
    private val timeout: Duration
) {

    suspend fun start() = coroutineScope {
        val outputStream = FileImageOutputStream(fileManager.createFile(FileType.SCREENSHOT, poolId, device.toDeviceInfo(), test))
        val writer = GifSequenceWriter(outputStream, TYPE_INT_ARGB, configuration.delayMs, true)
        var targetOrientation = detectCurrentDeviceOrientation()
        while (isActive) {
            val capturingTimeMillis = measureTimeMillis {
                getScreenshot(targetOrientation)?.let {
                    if (targetOrientation == UNDEFINED) {
                        // remember the target orientation
                        targetOrientation = it.getOrientation()
                    }
                    writer.writeToSequence(it)
                }
            }
            val sleepTimeMillis = when {
                (configuration.delayMs - capturingTimeMillis) < 0 -> 0
                else -> configuration.delayMs - capturingTimeMillis
            }
            delay(sleepTimeMillis)
        }
        writer.close()
        outputStream.close()
    }

    private fun detectCurrentDeviceOrientation(): Int {
        /**
         * `dumpsys input` is too slow for our purposes: by the time we get the response with SurfaceOrientation the test might already have
         * finished
         */
        return UNDEFINED
    }

    private suspend fun getScreenshot(targetOrientation: Rotation): RenderedImage? {
        return try {
            val screenshot = device.getScreenshot(timeout)?.let {
                // in case the orientation of the image is different than the target, rotate by 90 degrees
                if (it.getOrientation() != targetOrientation) {
                    Scalr.rotate(it, Scalr.Rotation.CW_90).also { org -> org.flush() }
                } else {
                    it
                }
            } ?: return null

            // the first time the orientation did not settle, use the actual image orientation
            val resolvedOrientation = if (targetOrientation == UNDEFINED) screenshot.getOrientation() else targetOrientation
            if (resolvedOrientation == PORTRAIT) {
                Scalr.resize(screenshot, Scalr.Method.SPEED, Scalr.Mode.AUTOMATIC, configuration.width, configuration.height)
            } else {
                Scalr.resize(screenshot, Scalr.Method.SPEED, Scalr.Mode.AUTOMATIC, configuration.height, configuration.width)
            }
        } catch (e: TimeoutException) {
            logger.error(e) { "Timeout. Exiting" }
            null
        } catch (e: IOException) {
            null
        } catch (e: CommandRejectedException) {
            logger.error(e) { "Adb is not responding. Exiting" }
            null
        }
    }

    /** retrieves the orientation of the RenderImage */
    private fun RenderedImage.getOrientation(): Int {
        return if (width > height) LANDSCAPE else PORTRAIT
    }

    companion object {
        val logger = MarathonLogging.logger(ScreenCapturer::class.java.simpleName)
        private const val UNDEFINED = 0
        private const val PORTRAIT = 1
        private const val LANDSCAPE = 2
    }
}
