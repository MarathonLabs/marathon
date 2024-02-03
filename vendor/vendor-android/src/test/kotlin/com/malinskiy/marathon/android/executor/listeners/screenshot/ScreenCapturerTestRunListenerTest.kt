package com.malinskiy.marathon.android.executor.listeners.screenshot

import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.server.junit5.AdbClient
import com.malinskiy.adam.server.junit5.AdbServer
import com.malinskiy.adam.server.junit5.AdbTest
import com.malinskiy.adam.server.stub.AndroidDebugBridgeServer
import com.malinskiy.marathon.android.adam.TestConfigurationFactory
import com.malinskiy.marathon.android.adam.TestDeviceFactory
import com.malinskiy.marathon.android.adam.boot
import com.malinskiy.marathon.android.adam.features
import com.malinskiy.marathon.android.adam.framebuffer
import com.malinskiy.marathon.android.model.TestIdentifier
import com.malinskiy.marathon.config.ScreenRecordingPolicy
import com.malinskiy.marathon.config.vendor.android.ScreenshotConfiguration
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.Attachment
import com.malinskiy.marathon.execution.AttachmentType
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FileType
import com.malinskiy.marathon.report.attachment.AttachmentListener
import com.malinskiy.marathon.test.TestBatch
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.time.Duration

@AdbTest
class ScreenCapturerTestRunListenerTest {
    @AdbClient
    lateinit var client: AndroidDebugBridgeClient

    @AdbServer
    lateinit var server: AndroidDebugBridgeServer

    @TempDir
    lateinit var temp: File

    @Test
    fun testDefault() {
        val configuration = TestConfigurationFactory.create()
        val device = TestDeviceFactory.create(client, configuration, mock())
        val fileManager = mock<FileManager>()
        val devicePoolId = DevicePoolId("testpool")
        val batch = TestBatch(listOf(test1.toTest()))
        val attachmentListener = mock<AttachmentListener>()
        val screenshot = File(temp, "screenshot")
        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot()
                    framebuffer(File(javaClass.getResource("/framebuffer/screencap_1.bin").toURI()))
                }
                features("emulator-5554")
            }

            device.setup()
            whenever(fileManager.createFile(FileType.SCREENSHOT, devicePoolId, device.toDeviceInfo(), test1.toTest(), batch.id))
                .thenReturn(screenshot)

            val listener = ScreenCapturerTestRunListener(
                fileManager,
                devicePoolId,
                batch.id,
                device,
                ScreenRecordingPolicy.ON_FAILURE,
                ScreenshotConfiguration(),
                Duration.ofMillis(300),
                this
            )
            listener.registerListener(attachmentListener)
            listener.testRunStarted("Testing", 1)
            listener.testStarted(test1)
            listener.testFailed(test1, "trace")
            listener.testEnded(test1, mapOf())

            verify(attachmentListener, times(1)).onAttachment(test1.toTest(), Attachment(screenshot, AttachmentType.SCREENSHOT_GIF))
        }
    }

    @Test
    fun testRunFailed() {
        val configuration = TestConfigurationFactory.create()
        val device = TestDeviceFactory.create(client, configuration, mock())
        val fileManager = mock<FileManager>()
        val devicePoolId = DevicePoolId("testpool")
        val batch = TestBatch(listOf(test1.toTest()))
        val attachmentListener = mock<AttachmentListener>()
        val screenshot = File(temp, "screenshot").apply { writeText("X") }
        val screenshotBatch = File(temp, "screenshot-batch")
        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot()
                    framebuffer(File(javaClass.getResource("/framebuffer/screencap_1.bin").toURI()))
                }
                features("emulator-5554")
            }

            device.setup()
            whenever(fileManager.createFile(FileType.SCREENSHOT, devicePoolId, device.toDeviceInfo(), test = test1.toTest(), testBatchId = batch.id))
                .thenReturn(screenshot)
            whenever(fileManager.createFile(FileType.SCREENSHOT, devicePoolId, device.toDeviceInfo(), testBatchId = batch.id))
                .thenReturn(screenshotBatch)

            val listener = ScreenCapturerTestRunListener(
                fileManager,
                devicePoolId,
                batch.id,
                device,
                ScreenRecordingPolicy.ON_FAILURE,
                ScreenshotConfiguration(),
                Duration.ofMillis(300),
                this
            )
            listener.registerListener(attachmentListener)
            listener.testRunStarted("Testing", 1)
            listener.testStarted(test1)
            delay(300)
            listener.testRunFailed("Problems are all around us")

            verify(fileManager, times(2)).createFile(FileType.SCREENSHOT, devicePoolId, device.toDeviceInfo(), test = test1.toTest(), testBatchId = batch.id)
            verify(fileManager, times(1)).createFile(FileType.SCREENSHOT, devicePoolId, device.toDeviceInfo(), testBatchId = batch.id)
        }
    }

    companion object {
        val test1 = TestIdentifier("com.example.Class", "method1")
    }
}
