package com.malinskiy.marathon.android.executor.listeners.screenshot

import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.ScreenshotConfiguration
import com.malinskiy.marathon.android.executor.listeners.NoOpTestRunListener
import com.malinskiy.marathon.android.model.TestIdentifier
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.Attachment
import com.malinskiy.marathon.execution.AttachmentType
import com.malinskiy.marathon.execution.policy.ScreenRecordingPolicy
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FileType
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.attachment.AttachmentListener
import com.malinskiy.marathon.report.attachment.AttachmentProvider
import com.malinskiy.marathon.test.toSimpleSafeTestName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlin.coroutines.CoroutineContext

class ScreenCapturerTestRunListener(
    private val fileManager: FileManager,
    private val pool: DevicePoolId,
    private val device: AndroidDevice,
    private val screenRecordingPolicy: ScreenRecordingPolicy,
    private val screenshotConfiguration: ScreenshotConfiguration,
    private val coroutineScope: CoroutineScope
) : NoOpTestRunListener(), CoroutineScope, AttachmentProvider {

    private val attachmentListeners = mutableListOf<AttachmentListener>()

    override fun registerListener(listener: AttachmentListener) {
        attachmentListeners.add(listener)
    }

    private var hasFailed: Boolean = false

    private var screenCapturerJob: Job? = null
    private val logger = MarathonLogging.logger(ScreenCapturerTestRunListener::class.java.simpleName)
    override val coroutineContext: CoroutineContext
        get() = coroutineScope.coroutineContext

    override suspend fun testStarted(test: TestIdentifier) {
        super.testStarted(test)
        hasFailed = false

        val toTest = test.toTest()
        logger.debug { "Starting recording for ${toTest.toSimpleSafeTestName()}" }

        screenCapturerJob = async {
            ScreenCapturer(device, pool, fileManager, toTest, screenshotConfiguration).start()
        }
    }

    override suspend fun testFailed(test: TestIdentifier, trace: String) {
        hasFailed = true
    }

    override suspend fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {
        super.testEnded(test, testMetrics)
        val toTest = test.toTest()
        logger.debug { "Finished recording for ${toTest.toSimpleSafeTestName()}" }
        screenCapturerJob?.cancel()

        if (!hasFailed && screenRecordingPolicy == ScreenRecordingPolicy.ON_FAILURE) {
            screenCapturerJob?.invokeOnCompletion {
                async {
                    fileManager.createFile(FileType.SCREENSHOT, pool, device.toDeviceInfo(), toTest).delete()
                }
            }
        } else {
            attachmentListeners.forEach {
                val file = fileManager.createFile(FileType.SCREENSHOT, pool, device.toDeviceInfo(), toTest)
                val attachment = Attachment(file, AttachmentType.SCREENSHOT)
                it.onAttachment(toTest, attachment)
            }
        }
    }
}
