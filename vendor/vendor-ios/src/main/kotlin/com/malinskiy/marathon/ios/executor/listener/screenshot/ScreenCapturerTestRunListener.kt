package com.malinskiy.marathon.ios.executor.listener.screenshot

import com.malinskiy.marathon.config.ScreenRecordingPolicy
import com.malinskiy.marathon.config.vendor.ios.ScreenshotConfiguration
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.screenshot.ScreenCapturer
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.Attachment
import com.malinskiy.marathon.execution.AttachmentType
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FileType
import com.malinskiy.marathon.ios.AppleDevice
import com.malinskiy.marathon.ios.executor.listener.AppleTestRunListener
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureReason
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.attachment.AttachmentProvider
import com.malinskiy.marathon.report.attachment.AttachmentProviderDelegate
import com.malinskiy.marathon.test.Test
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
    private val device: AppleDevice,
    private val screenRecordingPolicy: ScreenRecordingPolicy,
    private val screenshotConfiguration: ScreenshotConfiguration,
    private val timeout: Duration,
    coroutineScope: CoroutineScope,
    private val attachmentProvider: AttachmentProviderDelegate = AttachmentProviderDelegate(),
    ) : AppleTestRunListener, CoroutineScope by coroutineScope, AttachmentProvider by attachmentProvider {
    private var supervisorJob: Job? = null
    private val logger = MarathonLogging.logger(ScreenCapturerTestRunListener::class.java.simpleName)
    private val screenCapturer = ScreenCapturer(device.toDeviceInfo(), device, pool, testBatchId, fileManager, screenshotConfiguration.delay, screenshotConfiguration.height, screenshotConfiguration.width, timeout)
    private var lastTestIdentifier: Test? = null

    override suspend fun testStarted(test: Test) {
        lastTestIdentifier = test
        logger.debug { "Starting recording for ${test.toSimpleSafeTestName()}" }

        val supervisor = SupervisorJob()
        supervisorJob = supervisor
        async(supervisor) {
            screenCapturer.start(test)
        }
    }

    override suspend fun testFailed(test: Test, startTime: Long, endTime: Long, trace: String?) {
        testEnded(test, true)
    }

    override suspend fun testPassed(test: Test, startTime: Long, endTime: Long) {
        testEnded(test, false)
    }

    override suspend fun testIgnored(test: Test, startTime: Long, endTime: Long) {
        testEnded(test, false)
    }

    private suspend fun testEnded(test: Test, hasFailed: Boolean) {
        lastTestIdentifier = null
        logger.debug { "Finished recording for ${test.toSimpleSafeTestName()}" }
        supervisorJob?.cancelAndJoin()
        if (!hasFailed && screenRecordingPolicy == ScreenRecordingPolicy.ON_FAILURE) {
            supervisorJob?.invokeOnCompletion {
                fileManager.createFile(FileType.SCREENSHOT, pool, device.toDeviceInfo(), test, testBatchId).delete()
            }
        } else {
            val file = fileManager.createFile(FileType.SCREENSHOT, pool, device.toDeviceInfo(), test, testBatchId)
            if(screenCapturer.capturesTaken > 0) {
                attachmentProvider.onAttachment(test, Attachment(file, AttachmentType.SCREENSHOT_GIF))
            } else {
                file.delete()
            }
        }
    } 

    override suspend fun testRunFailed(errorMessage: String, reason: DeviceFailureReason) {
        supervisorJob?.cancelAndJoin()
        lastTestIdentifier?.let { id ->
            val existingRecording = fileManager.createFile(FileType.SCREENSHOT, pool, device.toDeviceInfo(), id, testBatchId)
            if (existingRecording.length() > 0) {
                //Moving existing recording for a test as a failure for a batch
                existingRecording.renameTo(fileManager.createFile(FileType.SCREENSHOT, pool, device.toDeviceInfo(), testBatchId))
            }
        }
    }
}
