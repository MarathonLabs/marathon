package com.malinskiy.marathon.android.executor.listeners.screenshot

import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.executor.listeners.NoOpTestRunListener
import com.malinskiy.marathon.android.model.TestIdentifier
import com.malinskiy.marathon.config.ScreenRecordingPolicy
import com.malinskiy.marathon.config.vendor.android.ScreenshotConfiguration
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.screenshot.ScreenCapturer
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.Attachment
import com.malinskiy.marathon.execution.AttachmentType
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FileType
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.attachment.AttachmentListener
import com.malinskiy.marathon.report.attachment.AttachmentProvider
import com.malinskiy.marathon.test.toSimpleSafeTestName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import java.time.Duration

class ScreenCapturerTestRunListener(
    private val fileManager: FileManager,
    private val pool: DevicePoolId,
    private val testBatchId: String,
    private val device: AndroidDevice,
    private val screenRecordingPolicy: ScreenRecordingPolicy,
    private val screenshotConfiguration: ScreenshotConfiguration,
    private val timeout: Duration,
    coroutineScope: CoroutineScope
) : NoOpTestRunListener(), CoroutineScope by coroutineScope, AttachmentProvider {


    private val attachmentListeners = mutableListOf<AttachmentListener>()

    override fun registerListener(listener: AttachmentListener) {
        attachmentListeners.add(listener)
    }

    private var supervisorJob: Job? = null
    private var hasFailed: Boolean = false
    private val screenCapturer = ScreenCapturer(
        device.toDeviceInfo(),
        device,
        pool,
        testBatchId,
        fileManager,
        Duration.ofMillis(screenshotConfiguration.delayMs.toLong()),
        screenshotConfiguration.height,
        screenshotConfiguration.width,
        timeout
    )
    private val logger = MarathonLogging.logger(ScreenCapturerTestRunListener::class.java.simpleName)
    private var lastTestIdentifier: TestIdentifier? = null

    override suspend fun testStarted(test: TestIdentifier) {
        super.testStarted(test)
        hasFailed = false

        val toTest = test.toTest()
        lastTestIdentifier = test
        logger.debug { "Starting recording for ${toTest.toSimpleSafeTestName()}" }

        val supervisor = SupervisorJob()
        supervisorJob = supervisor
        async(supervisor) {
            screenCapturer.start(toTest)
        }
    }

    override suspend fun testFailed(test: TestIdentifier, trace: String) {
        hasFailed = true
    }

    override suspend fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {
        super.testEnded(test, testMetrics)
        val toTest = test.toTest()
        lastTestIdentifier = null
        logger.debug { "Finished recording for ${toTest.toSimpleSafeTestName()}" }
        supervisorJob?.cancelAndJoin()
        if (!hasFailed && screenRecordingPolicy == ScreenRecordingPolicy.ON_FAILURE) {
            supervisorJob?.invokeOnCompletion {
                fileManager.createFile(FileType.SCREENSHOT, pool, device.toDeviceInfo(), toTest, testBatchId).delete()
            }
        } else {
            attachmentListeners.forEach {
                val file = fileManager.createFile(FileType.SCREENSHOT, pool, device.toDeviceInfo(), toTest, testBatchId)
                val attachment = Attachment(file, AttachmentType.SCREENSHOT_GIF, "screen")
                it.onAttachment(toTest, attachment)
            }
        }
    }

    override suspend fun testRunFailed(errorMessage: String) {
        super.testRunFailed(errorMessage)
        /**
         * We might not observe the testEnded event, but the testRunFailed will always be reported
         */
        supervisorJob?.cancelAndJoin()
        lastTestIdentifier?.let { id ->
            val existingRecording = fileManager.createFile(FileType.SCREENSHOT, pool, device.toDeviceInfo(), id.toTest(), testBatchId)
            if (existingRecording.length() > 0) {
                //Moving existing recording for a test as a failure for a batch
                existingRecording.renameTo(fileManager.createFile(FileType.SCREENSHOT, pool, device.toDeviceInfo(), testBatchId = testBatchId))
            }
        }
    }
}
