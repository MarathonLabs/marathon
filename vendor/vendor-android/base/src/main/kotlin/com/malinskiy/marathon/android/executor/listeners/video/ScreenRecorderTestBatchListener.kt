package com.malinskiy.marathon.android.executor.listeners.video

import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.VideoConfiguration
import com.malinskiy.marathon.android.exception.TransferException
import com.malinskiy.marathon.android.executor.listeners.NoOpTestRunListener
import com.malinskiy.marathon.android.model.TestIdentifier
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.Attachment
import com.malinskiy.marathon.execution.AttachmentType
import com.malinskiy.marathon.execution.policy.ScreenRecordingPolicy
import com.malinskiy.marathon.extension.withTimeoutOrNull
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
import java.time.Duration
import kotlin.system.measureTimeMillis

class ScreenRecorderTestBatchListener(
    private val fileManager: FileManager,
    private val pool: DevicePoolId,
    private val testBatchId: String,
    private val device: AndroidDevice,
    private val videoConfiguration: VideoConfiguration,
    private val screenRecordingPolicy: ScreenRecordingPolicy,
    coroutineScope: CoroutineScope
) : NoOpTestRunListener(), AttachmentProvider, CoroutineScope by coroutineScope {

    val attachmentListeners = mutableListOf<AttachmentListener>()

    override fun registerListener(listener: AttachmentListener) {
        attachmentListeners.add(listener)
    }

    private val logger = MarathonLogging.logger("ScreenRecorder")

    private val screenRecorderStopper = ScreenRecorderStopper(device)

    private var hasFailed: Boolean = false
    private var supervisorJob: Job? = null
    private var lastRemoteFile: String? = null

    override suspend fun testStarted(test: TestIdentifier) {
        hasFailed = false

        val remoteFile = device.fileManager.remoteVideoForTest(test.toTest(), testBatchId)
        lastRemoteFile = remoteFile
        val screenRecorder = ScreenRecorder(device, videoConfiguration, remoteFile)
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
        //Treating as failure
        hasFailed = true
    }

    override suspend fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {
        pullVideo(test.toTest())
        lastRemoteFile = null
    }

    override suspend fun testRunFailed(errorMessage: String) {
        try {
            if (device.verifyHealthy()) {
                stop()
                lastRemoteFile?.let {
                    pullLastBatchVideo(it)
                    removeRemoteVideo(it)
                }
            }
        } catch (e: InterruptedException) {
            logger.warn { "Can't stop recording" }
        } catch (e: TransferException) {
            logger.warn { "Can't pull video" }
        }
    }

    private suspend fun stop() {
        val stop = measureTimeMillis {
            screenRecorderStopper.stopScreenRecord()
        }
        logger.trace { "stop ${stop}ms" }
        val join = measureTimeMillis {
            logger.trace { "cancel" }
            supervisorJob?.cancelAndJoin()
        }
        logger.trace { "join ${join}ms" }
    }

    /**
     * @throws TransferException in case there are any issues pulling the file
     */
    private suspend fun pullVideo(test: Test) {
        try {
            stop()
            if (screenRecordingPolicy == ScreenRecordingPolicy.ON_ANY || hasFailed) {
                pullTestVideo(test)
            }
            removeRemoteVideo(device.fileManager.remoteVideoForTest(test, testBatchId))
        } catch (e: InterruptedException) {
            logger.warn { "Can't stop recording" }
        } catch (e: TransferException) {
            logger.warn { "Can't pull video" }
        }
    }

    private suspend fun pullTestVideo(test: Test) {
        val localVideoFile = fileManager.createFile(FileType.VIDEO, pool, device.toDeviceInfo(), test, testBatchId)
        val remoteFilePath = device.fileManager.remoteVideoForTest(test, testBatchId)
        val millis = measureTimeMillis {
            device.safePullFile(remoteFilePath, localVideoFile.toString())
        }
        logger.trace { "Pulling finished in ${millis}ms $remoteFilePath " }
        attachmentListeners.forEach { it.onAttachment(test, Attachment(localVideoFile, AttachmentType.VIDEO)) }
    }

    /**
     * This can be called both when test times out and device unavailable
     */
    private suspend fun pullLastBatchVideo(remoteFilePath: String) {
        val localVideoFile = fileManager.createFile(FileType.VIDEO, pool, device.toDeviceInfo(), testBatchId)
        val millis = measureTimeMillis {
            device.safePullFile(remoteFilePath, localVideoFile.toString())
        }
        logger.trace { "Pulling finished in ${millis}ms $remoteFilePath " }
    }

    private suspend fun removeRemoteVideo(remoteFilePath: String) {
        val millis = measureTimeMillis {
            device.fileManager.removeRemotePath(remoteFilePath)
        }
        logger.trace { "Removed file in ${millis}ms $remoteFilePath" }
    }
}

private suspend fun AndroidDevice.verifyHealthy(): Boolean {
    return withTimeoutOrNull(Duration.ofSeconds(10)) {
        executeShellCommand("echo quickhealthcheck")?.let { it.contains("quickhealthcheck") } ?: false
    } ?: false
}
