package com.malinskiy.marathon.android.executor.listeners.screenshot

import com.malinskiy.marathon.android.AndroidDevice
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
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.imageio.stream.FileImageOutputStream
import kotlin.system.measureTimeMillis


class ScreenCapturer(
    val device: AndroidDevice,
    private val poolId: DevicePoolId,
    private val fileManager: FileManager,
    val test: Test
) {

    suspend fun start() = coroutineScope {
        val outputStream = FileImageOutputStream(fileManager.createFile(FileType.SCREENSHOT, poolId, device.toDeviceInfo(), test))
        val writer = GifSequenceWriter(outputStream, TYPE_INT_ARGB, DELAY, true)
        while (isActive) {
            val capturingTimeMillis = measureTimeMillis {
                getScreenshot()?.let { writer.writeToSequence(it) }
            }
            val sleepTimeMillis = when {
                (DELAY - capturingTimeMillis) < 0 -> 0
                else -> DELAY - capturingTimeMillis
            }
            delay(sleepTimeMillis)
        }
        writer.close()
        outputStream.close()
    }

    private fun getScreenshot(): RenderedImage? {
        return try {
            val screenshot = device.getScreenshot(TIMEOUT_MS, TimeUnit.MILLISECONDS)
            Scalr.resize(screenshot, Scalr.Method.SPEED, Scalr.Mode.AUTOMATIC, 1280, 720)
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
        const val DELAY = 500
        const val TIMEOUT_MS = 300L
        val logger = MarathonLogging.logger(ScreenCapturer::class.java.simpleName)
    }
}