package com.malinskiy.marathon.android.executor.listeners.screenshot

import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.ScreenshotConfiguration
import com.malinskiy.marathon.android.exception.CommandRejectedException
import com.malinskiy.marathon.android.executor.listeners.screenshot.gif.GifEncoderKotlin
import com.malinskiy.marathon.android.extension.convert
import com.malinskiy.marathon.android.model.Rotation
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FileType
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.imgscalr.Scalr
import java.awt.image.BufferedImage
import java.io.IOException
import java.time.Duration
import java.util.concurrent.TimeoutException
import kotlin.coroutines.coroutineContext
import kotlin.system.measureTimeMillis


class ScreenCapturer(
    val device: AndroidDevice,
    private val poolId: DevicePoolId,
    private val fileManager: FileManager,
    private val configuration: ScreenshotConfiguration,
    private val timeout: Duration
) {

    suspend fun start(test: Test) {
        val targetRotation = detectCurrentDeviceOrientation()

        val writer: GifEncoderKotlin? = null
        var writeChannel: ByteWriteChannel? = null
        var frameCount = 0
        val file = fileManager.createFile(FileType.SCREENSHOT, poolId, device.toDeviceInfo(), test)
        try {
            writeChannel = file.writeChannel()
            val writer = GifEncoderKotlin()
            writer.delay = configuration.delayMs.toShort()
            writer.start(writeChannel)
            while (coroutineContext.isActive) {
                val capturingTimeMillis = measureTimeMillis {
                    getScreenshot(targetRotation)?.let {
                        val success = writer.addFrame(
                            it.convert(BufferedImage.TYPE_3BYTE_BGR),
                            it.width.toShort(),
                            it.height.toShort(),
                            writeChannel
                        )
                        if (success) {
                            frameCount++
                        }
                    }
                }
                val sleepTimeMillis = when {
                    (configuration.delayMs - capturingTimeMillis) < 0 -> 0
                    else -> configuration.delayMs - capturingTimeMillis
                }
                delay(sleepTimeMillis)
            }
        } finally {
            withContext(NonCancellable) {
                writeChannel?.let {
                    writer?.finish(it)
                    it.close()
                }
                if (frameCount == 0) {
                    file.delete()
                }
            }
        }
    }

    private fun detectCurrentDeviceOrientation() = device.initialRotation

    private suspend fun getScreenshot(targetOrientation: Rotation): BufferedImage? {
        return try {
            val screenshot = device.getScreenshot(timeout)?.let {
                /**
                 * using initial device rotation during the device discovery,
                 * dumpsys input takes too long to retrieve this in realtime
                 */
                when (targetOrientation) {
                    Rotation.ROTATION_0 -> it
                    Rotation.ROTATION_180 -> Scalr.rotate(it, Scalr.Rotation.CW_180).also { org -> org.flush() }
                    Rotation.ROTATION_270 -> Scalr.rotate(it, Scalr.Rotation.CW_270).also { org -> org.flush() }
                    Rotation.ROTATION_90 -> Scalr.rotate(it, Scalr.Rotation.CW_90).also { org -> org.flush() }
                }
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
