package com.malinskiy.marathon.android.executor.listeners.screenshot

import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.executor.listeners.NoOpTestRunListener
import com.malinskiy.marathon.android.model.TestIdentifier
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.Attachment
import com.malinskiy.marathon.execution.AttachmentType
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FileType
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.attachment.AttachmentListener
import com.malinskiy.marathon.report.attachment.AttachmentProvider
import java.util.UUID

class AdamScreenCaptureTestRunListener(
    private val pool: DevicePoolId,
    private val device: AndroidDevice,
    private val fileManager: FileManager,
    private val testBatchId: String,
) : NoOpTestRunListener(), AttachmentProvider {
    private val logger = MarathonLogging.logger(ScreenCapturerTestRunListener::class.java.simpleName)
    private val attachmentListeners = mutableListOf<AttachmentListener>()

    override suspend fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {
        super.testEnded(test, testMetrics)

        val screenshots = testMetrics.filterKeys { it == "com.malinskiy.adam.junit4.android.screencapture.AdamScreenCaptureProcessor.v1" }
        screenshots.values.forEach { path ->
            val extension = path.substringAfterLast('.')
            var attachmentType: AttachmentType? = null
            var fileType: FileType? = null
            when (extension) {
                "jpeg", "jpg" -> {
                    attachmentType = AttachmentType.SCREENSHOT_JPEG
                    fileType = FileType.SCREENSHOT_JPG
                }

                "png" -> {
                    attachmentType = AttachmentType.SCREENSHOT_PNG
                    fileType = FileType.SCREENSHOT_PNG
                }

                "webp" -> {
                    attachmentType = AttachmentType.SCREENSHOT_WEBP
                    fileType = FileType.SCREENSHOT_WEBP
                }

                "gif" -> {
                    attachmentType = AttachmentType.SCREENSHOT_GIF
                    fileType = FileType.SCREENSHOT_GIF
                }

                else -> Unit
            }

            if (attachmentType != null && fileType != null) {
                val localFile = fileManager.createFile(
                    fileType,
                    pool,
                    device.toDeviceInfo(),
                    test.toTest(),
                    testBatchId = testBatchId,
                    id = UUID.randomUUID().toString()
                )
                device.safePullFile(path, localFile.absolutePath)
                logger.debug { "Received screen capture file $path" }
                attachmentListeners.forEach {
                    it.onAttachment(test.toTest(), Attachment(localFile, attachmentType, Attachment.Name.SCREEN))
                }
            } else {
                logger.warn { "Unable to decode image format of screen capture $path. The file will be available in the report directory, but will not be included as part of any visual report" }
            }
        }
    }

    override fun registerListener(listener: AttachmentListener) {
        attachmentListeners.add(listener)
    }
}
