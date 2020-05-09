package com.malinskiy.marathon.android.executor.listeners.video

import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.exception.TransferException
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
import com.malinskiy.marathon.test.Test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlin.coroutines.CoroutineContext
import kotlin.system.measureTimeMillis

class ScreenRecorderTestRunListener(
    private val fileManager: FileManager,
    private val pool: DevicePoolId,
    private val device: AndroidDevice,
    private val screenRecordingPolicy: ScreenRecordingPolicy,
    private val coroutineScope: CoroutineScope
) : NoOpTestRunListener(), AttachmentProvider, CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = coroutineScope.coroutineContext

    val attachmentListeners = mutableListOf<AttachmentListener>()

    override fun registerListener(listener: AttachmentListener) {
        attachmentListeners.add(listener)
    }

    private val logger = MarathonLogging.logger("ScreenRecorder")

    private val screenRecorderStopper = ScreenRecorderStopper(device)

    private var hasFailed: Boolean = false
    private var supervisorJob: Job? = null

    override suspend fun testStarted(test: TestIdentifier) {
        hasFailed = false

        val screenRecorder = ScreenRecorder(device, device.fileManager.remoteVideoForTest(test.toTest()))
        val supervisor = SupervisorJob()
        supervisorJob = supervisor
        async(supervisor) {
            screenRecorder.run()
        }
    }

    override suspend fun testFailed(test: TestIdentifier, trace: String) {
        hasFailed = true
    }

    override suspend fun testAssumptionFailure(test: TestIdentifier, trace: String) {
        pullVideo(test.toTest())
    }

    override suspend fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {
        pullVideo(test.toTest())
    }

    private suspend fun pullVideo(test: Test) {
        try {
            val join = measureTimeMillis {
                logger.trace { "cancel" }
                supervisorJob?.cancelAndJoin()
            }
            logger.trace { "join ${join}ms" }
            if (screenRecordingPolicy == ScreenRecordingPolicy.ON_ANY || hasFailed) {
                val stop = measureTimeMillis {
                    screenRecorderStopper.stopScreenRecord()
                }
                logger.trace { "stop ${stop}ms" }
                pullTestVideo(test)
            }
            removeTestVideo(test)
        } catch (e: InterruptedException) {
            logger.warn { "Can't stop recording" }
        } catch (e: TransferException) {
            logger.warn { "Can't pull video" }
        }
    }

    private suspend fun pullTestVideo(test: Test) {
        val localVideoFile = fileManager.createFile(FileType.VIDEO, pool, device.toDeviceInfo(), test)
        val remoteFilePath = device.fileManager.remoteVideoForTest(test)
        val millis = measureTimeMillis {
            device.pullFile(remoteFilePath, localVideoFile.toString())
        }
        logger.trace { "Pulling finished in ${millis}ms $remoteFilePath " }
        attachmentListeners.forEach { it.onAttachment(test, Attachment(localVideoFile, AttachmentType.VIDEO)) }
    }

    private suspend fun removeTestVideo(test: Test) {
        val remoteFilePath = device.fileManager.remoteVideoForTest(test)
        val millis = measureTimeMillis {
            device.fileManager.removeRemotePath(remoteFilePath)
        }
        logger.trace { "Removed file in ${millis}ms $remoteFilePath" }
    }
}
