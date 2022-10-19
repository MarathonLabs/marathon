package com.malinskiy.marathon.android.executor.listeners.screenshot

import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.executor.listeners.NoOpTestRunListener
import com.malinskiy.marathon.android.model.TestIdentifier
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.Attachment
import com.malinskiy.marathon.execution.AttachmentType
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.attachment.AttachmentListener
import com.malinskiy.marathon.report.attachment.AttachmentProvider

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
            val attachmentType = when (extension) {
                "jpeg", "jpg" -> AttachmentType.SCREENSHOT_JPEG
                "png" -> AttachmentType.SCREENSHOT_PNG
                "webp" -> AttachmentType.SCREENSHOT_WEBP
                else -> null
            }
            if (attachmentType != null) {
                val localFile = fileManager.createScreenshotFile(extension, pool, device.toDeviceInfo(), test.toTest(), testBatchId)
                device.safePullFile(path, localFile.absolutePath)
                logger.debug { "Received screen capture file $path" }
                attachmentListeners.forEach {
                    it.onAttachment(test.toTest(), Attachment(localFile, attachmentType))
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
