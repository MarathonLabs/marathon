package com.malinskiy.marathon.device.screenshot

import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FileType
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toSimpleSafeTestName
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.imgscalr.Scalr
import java.awt.image.BufferedImage
import java.awt.image.RenderedImage
import java.io.IOException
import java.time.Duration
import java.util.concurrent.TimeoutException
import javax.imageio.stream.FileImageOutputStream
import kotlin.coroutines.coroutineContext
import kotlin.system.measureTimeMillis

class ScreenCapturer(
    val deviceInfo: DeviceInfo,
    private val screenshottable: Screenshottable,
    private val poolId: DevicePoolId,
    private val testBatchId: String,
    private val fileManager: FileManager,
    private val delay: Duration,
    private val height: Int,
    private val width: Int,
    private val timeout: Duration
) {
    private val delayMillis by lazy { delay.toMillis().toInt() }
    var capturesTaken: Int = 0

    // A miss is a frame that came back empty: adam's capture returns null on timeout, and IO errors
    // are swallowed per frame. Without these counters a capturer that never gets a frame leaves a
    // 1-byte GIF (which the report hides) and nothing in the log.
    private var capturesMissed: Int = 0
    private var captureMillis: Long = 0
    private var lastError: Throwable? = null

    suspend fun start(test: Test) {
        var outputStream: FileImageOutputStream? = null
        var writer: GifSequenceWriter? = null
        capturesTaken = 0
        capturesMissed = 0
        captureMillis = 0
        lastError = null
        try {
            outputStream =
                FileImageOutputStream(fileManager.createFile(FileType.SCREENSHOT, poolId, deviceInfo, test, testBatchId))
            writer = GifSequenceWriter(outputStream, BufferedImage.TYPE_INT_ARGB, delayMillis, true)
            var targetRotation = detectCurrentDeviceOrientation()
            while (coroutineContext.isActive) {
                val capturingTimeMillis = measureTimeMillis {
                    val image = getScreenshot(targetRotation)
                    if (image != null) {
                        writer.writeToSequence(image)
                        capturesTaken++
                    } else {
                        capturesMissed++
                    }
                }
                captureMillis += capturingTimeMillis
                val sleepTimeMillis = when {
                    (delayMillis - capturingTimeMillis) < 0 -> 0
                    else -> delayMillis - capturingTimeMillis
                }
                delay(sleepTimeMillis)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // start() runs in an async nobody awaits, so an escaping exception would vanish.
            lastError = e
        } finally {
            writer?.close()
            outputStream?.close()
            logSummary(test)
        }
    }

    private fun logSummary(test: Test) {
        val attempts = capturesTaken + capturesMissed
        val avgMillis = if (attempts > 0) captureMillis / attempts else 0
        val summary = "Screen capture for ${test.toSimpleSafeTestName()} on ${deviceInfo.serialNumber}: " +
            "$capturesTaken frames, $capturesMissed missed, avg ${avgMillis}ms per attempt (timeout ${timeout.toMillis()}ms)"
        if (capturesTaken == 0) {
            logger.warn(lastError) { "$summary. No frames captured, so no screen recording will be attached" }
        } else {
            logger.debug { summary }
        }
    }

    private fun detectCurrentDeviceOrientation() = screenshottable.orientation

    /**
     * Field to detect dynamic screen rotations
     */
    private var firstFrameWidth: Int? = null
    private var firstFrameHeight: Int? = null

    private suspend fun getScreenshot(targetOrientation: Rotation): RenderedImage? {
        return try {
            val screenshot = screenshottable.getScreenshot(timeout)?.let { image ->
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
                Scalr.resize(screenshot, Scalr.Method.SPEED, Scalr.Mode.AUTOMATIC, width, height)
            } else {
                Scalr.resize(screenshot, Scalr.Method.SPEED, Scalr.Mode.AUTOMATIC, height, width)
            }
        } catch (e: TimeoutException) {
            lastError = e
            null
        } catch (e: IOException) {
            lastError = e
            null
        }
    }

    companion object {
        val logger = MarathonLogging.logger {}
    }
}
