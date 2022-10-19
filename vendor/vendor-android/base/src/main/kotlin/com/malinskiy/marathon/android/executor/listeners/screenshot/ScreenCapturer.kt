package com.malinskiy.marathon.android.executor.listeners.screenshot

import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.exception.CommandRejectedException
import com.malinskiy.marathon.android.model.Rotation
import com.malinskiy.marathon.config.vendor.android.ScreenshotConfiguration
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FileType
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.imgscalr.Scalr
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.awt.image.RenderedImage
import java.io.IOException
import java.time.Duration
import java.util.concurrent.TimeoutException
import javax.imageio.stream.FileImageOutputStream
import kotlin.coroutines.coroutineContext
import kotlin.system.measureTimeMillis


class ScreenCapturer(
    val device: AndroidDevice,
    private val poolId: DevicePoolId,
    private val testBatchId: String,
    private val fileManager: FileManager,
    private val configuration: ScreenshotConfiguration,
    private val timeout: Duration
) {

    suspend fun start(test: Test) {
        var outputStream: FileImageOutputStream? = null
        var writer: GifSequenceWriter? = null
        try {
            outputStream =
                FileImageOutputStream(fileManager.createFile(FileType.SCREENSHOT, poolId, device.toDeviceInfo(), test, testBatchId))
            writer = GifSequenceWriter(outputStream, TYPE_INT_ARGB, configuration.delayMs, true)
            var targetRotation = detectCurrentDeviceOrientation()
            while (coroutineContext.isActive) {
                val capturingTimeMillis = measureTimeMillis {
                    getScreenshot(targetRotation)?.let {
                        writer.writeToSequence(it)
                    }
                }
                val sleepTimeMillis = when {
                    (configuration.delayMs - capturingTimeMillis) < 0 -> 0
                    else -> configuration.delayMs - capturingTimeMillis
                }
                delay(sleepTimeMillis)
            }
        } finally {
            writer?.close()
            outputStream?.close()
        }
    }

    private fun detectCurrentDeviceOrientation() = device.initialRotation

    /**
     * Field to detect dynamic screen rotations
     */
    private var firstFrameWidth: Int? = null
    private var firstFrameHeight: Int? = null

    private suspend fun getScreenshot(targetOrientation: Rotation): RenderedImage? {
        return try {
            val screenshot = device.getScreenshot(timeout)?.let { image ->
                if (firstFrameWidth == null) firstFrameWidth = image.width
                if (firstFrameHeight == null) firstFrameHeight = image.height

                /**
                 * using initial device rotation during the device discovery,
                 * dumpsys input takes too long to retrieve this in realtime
                 *
                 * need to account for the first frame orientation as well
                 */

                var rotation = when (targetOrientation) {
                    Rotation.ROTATION_0 -> null
                    Rotation.ROTATION_180 -> Scalr.Rotation.CW_180
                    Rotation.ROTATION_270 -> Scalr.Rotation.CW_270
                    Rotation.ROTATION_90 -> Scalr.Rotation.CW_90
                }
                if(image.width == firstFrameHeight && image.height == firstFrameWidth) {
                    /**
                     * Runtime display rotation. No idea which direction, so just rotate by Pi/2 
                     */
                    rotation = when(rotation) {
                        Scalr.Rotation.CW_90 -> Scalr.Rotation.CW_180
                        Scalr.Rotation.CW_180 -> Scalr.Rotation.CW_270
                        Scalr.Rotation.CW_270 -> null
                        else -> Scalr.Rotation.CW_90
                    }
                }

                rotation?.let {
                    Scalr.rotate(image, it).also { org -> org.flush() }
                } ?: image
            } ?: return null
            
            if (screenshot.width < screenshot.height) {
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

    companion object {
        val logger = MarathonLogging.logger(ScreenCapturer::class.java.simpleName)
    }
}
