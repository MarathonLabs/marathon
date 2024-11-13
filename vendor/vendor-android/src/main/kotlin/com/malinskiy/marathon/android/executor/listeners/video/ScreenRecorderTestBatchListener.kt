package com.malinskiy.marathon.android.executor.listeners.video

import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.exceptions.TransferException
import com.malinskiy.marathon.android.executor.listeners.NoOpTestRunListener
import com.malinskiy.marathon.android.model.TestIdentifier
import com.malinskiy.marathon.config.ScreenRecordingPolicy
import com.malinskiy.marathon.config.vendor.android.VideoConfiguration
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.Attachment
import com.malinskiy.marathon.execution.AttachmentType
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
import java.io.File
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

    private val attachmentListeners = mutableListOf<AttachmentListener>()

    override fun registerListener(listener: AttachmentListener) {
        attachmentListeners.add(listener)
    }

    private val logger = MarathonLogging.logger("ScreenRecorderTestBatchListener")

    private val screenRecorder = ScreenRecorder(device, videoConfiguration)

    private var hasFailed: Boolean = false
    private var supervisorJob: Job? = null
    private var lastRemoteFiles: MutableList<String> = mutableListOf()
    private val chunkedRecording = device.apiLevel < 34
    private var chunkMap: HashMap<TestIdentifier, Long> = HashMap<TestIdentifier, Long>().apply { withDefault { 0 } }
    @Volatile
    private var cancelling: Boolean = false

    override suspend fun testStarted(test: TestIdentifier) {
        chunkMap[test] = 0
        hasFailed = false
        cancelling = false

        val remoteFile = remoteVideoForTest(test.toTest(), testBatchId, chunkMap[test])
        lastRemoteFiles = mutableListOf(remoteFile)

        val supervisor = SupervisorJob()
        supervisorJob = supervisor
        async(supervisor) {
            screenRecorder.run(remoteFile)
            while (chunkedRecording && !cancelling) {
                chunkMap[test] = chunkMap[test]?.inc() ?: 0
                val remoteFile = remoteVideoForTest(test.toTest(), testBatchId, chunkMap[test])
                lastRemoteFiles.add(remoteFile)
                screenRecorder.run(remoteFile)
            }
        }
    }

    private fun remoteVideoForTest(test: Test, testBatchId: String, chunkId: Long? = null): String {
        return if (chunkedRecording && chunkId != null) {
            device.fileManager.remoteChunkedVideoForTest(test, testBatchId, chunkId)
        } else {
            device.fileManager.remoteVideoForTest(test, testBatchId)
        }
    }

    override suspend fun testFailed(test: TestIdentifier, trace: String) {
        cancelNewInvocations()
        hasFailed = true
    }

    override suspend fun testAssumptionFailure(test: TestIdentifier, trace: String) {
        cancelNewInvocations()
        //Treating as failure
        hasFailed = true
    }

    override suspend fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {
        cancelNewInvocations()
        pullVideo(test)
        lastRemoteFiles.clear()
    }

    override suspend fun testRunFailed(errorMessage: String) {
        cancelNewInvocations()
        try {
            if (device.verifyHealthy()) {
                stop()
                pullLastBatchVideo(lastRemoteFiles)
                lastRemoteFiles.forEach {
                    removeRemoteVideoFile(it)
                }
            }
        } catch (e: InterruptedException) {
            logger.warn { "Can't stop recording" }
        } catch (e: TransferException) {
            logger.warn { "Can't pull video" }
        }
    }

    private suspend fun stop() {
        cancelNewInvocations()
        val stop = measureTimeMillis {
            screenRecorder.stopScreenRecord()
        }
        logger.trace { "stop ${stop}ms" }
        val join = measureTimeMillis {
            logger.trace { "cancel" }
            supervisorJob?.cancelAndJoin()
        }
        logger.trace { "join ${join}ms" }
    }

    /**
     * This prevents new invocations of screenrecord
     * Why:
     * - Cancelling the coroutine will corrupt the file
     * - Stopping screenrecorder will force a new screen recording
     */
    private fun cancelNewInvocations() {
        cancelling = true
    }

    /**
     * @throws TransferException in case there are any issues pulling the file
     */
    private suspend fun pullVideo(test: TestIdentifier) {
        try {
            stop()
            if (screenRecordingPolicy == ScreenRecordingPolicy.ON_ANY || hasFailed) {
                pullTestVideo(test)
            }
            removeRemoteVideo(test)
        } catch (e: InterruptedException) {
            logger.warn { "Can't stop recording" }
        } catch (e: TransferException) {
            logger.warn { "Can't pull video" }
        }
    }

    private suspend fun pullTestVideo(testIdentifier: TestIdentifier) {
        val test = testIdentifier.toTest()
        if (chunkedRecording) {
            val chunks = chunkMap[testIdentifier] ?: 0L
            for (i in 0..chunks) {
                val localVideoFile = fileManager.createFile(FileType.VIDEO, pool, device.toDeviceInfo(), test, testBatchId, chunk = i.toString())
                val remoteFilePath = remoteVideoForTest(test, testBatchId, i)
                pullTestVideoFile(remoteFilePath, localVideoFile, test)
                attachmentListeners.forEach { it.onAttachment(test, Attachment(localVideoFile, AttachmentType.VIDEO, name = "${Attachment.Name.SCREEN}-$i")) }
            }
        } else {
            val localVideoFile = fileManager.createFile(FileType.VIDEO, pool, device.toDeviceInfo(), test, testBatchId)
            val remoteFilePath = device.fileManager.remoteVideoForTest(test, testBatchId)
            pullTestVideoFile(remoteFilePath, localVideoFile, test)
            attachmentListeners.forEach { it.onAttachment(test, Attachment(localVideoFile, AttachmentType.VIDEO, Attachment.Name.SCREEN)) }
        }
    }

    private suspend fun pullTestVideoFile(remoteFilePath: String, localVideoFile: File, test: Test) {
        val millis = measureTimeMillis {
            device.safePullFile(remoteFilePath, localVideoFile.toString())
        }
        logger.trace { "Pulling finished in ${millis}ms $remoteFilePath " }
    }

    /**
     * This can be called both when test times out and device unavailable
     */
    private suspend fun pullLastBatchVideo(remoteFilePath: List<String>) {
        remoteFilePath.forEachIndexed { index, remoteChunkPath ->
            /**
             * List is ordered so the same chunked order is valid for batch videos
             */
            val localVideoFile = fileManager.createFile(FileType.VIDEO, pool, device.toDeviceInfo(), testBatchId = testBatchId, chunk = index.toString())
            val millis = measureTimeMillis {
                device.safePullFile(remoteChunkPath, localVideoFile.toString())
            }
            logger.trace { "Pulling finished in ${millis}ms $remoteChunkPath " }
        }
    }

    private suspend fun removeRemoteVideo(testIdentifier: TestIdentifier) {
        val test = testIdentifier.toTest()
        if (chunkedRecording) {
            val chunks = chunkMap[testIdentifier] ?: 0L
            for (i in 0..chunks) {
                val remoteFilePath = remoteVideoForTest(test, testBatchId, i)
                removeRemoteVideoFile(remoteFilePath)
            }
        } else {
            val remoteFilePath = device.fileManager.remoteVideoForTest(test, testBatchId)
            removeRemoteVideoFile(remoteFilePath)
        }
    }

    private suspend fun removeRemoteVideoFile(remoteFilePath: String) {
        val millis = measureTimeMillis {
            device.fileManager.removeRemotePath(remoteFilePath)
        }
        logger.trace { "Removed file in ${millis}ms $remoteFilePath" }
    }
}

private suspend fun AndroidDevice.verifyHealthy(): Boolean {
    return withTimeoutOrNull(Duration.ofSeconds(10)) {
        executeShellCommand("echo quickhealthcheck")?.output?.contains("quickhealthcheck") ?: false
    } ?: false
}
