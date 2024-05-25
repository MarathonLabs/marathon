package com.malinskiy.marathon.android.executor.listeners.video

import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.exceptions.TransferException
import com.malinskiy.marathon.android.executor.listeners.NoOpTestRunListener
import com.malinskiy.marathon.android.executor.listeners.video.ScreenRecorder.Companion.addFileNumberForVideo
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
import com.malinskiy.marathon.test.toSafeTestName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import java.io.File
import java.time.Duration
import java.util.UUID
import java.util.concurrent.TimeUnit
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
    private var lastUUID: UUID? = null

    private val testStartTimeMap: MutableMap<String, Long> = mutableMapOf()
    private val defaultTimeLimit: Long = 180

    override suspend fun testStarted(test: TestIdentifier) {
        hasFailed = false

        val remoteFile = device.fileManager.remoteVideoForTest(test.toTest(), testBatchId)
        lastRemoteFile = remoteFile
        val screenRecorder = ScreenRecorder(device, videoConfiguration, remoteFile)
        val supervisor = SupervisorJob()
        supervisorJob = supervisor
        async(supervisor) {
            val startTime = System.currentTimeMillis()
            with(UUID.randomUUID()) {  //safety for immediately failed run
                lastUUID = this
                testStartTimeMap[this.toString()] = startTime
            }
            testStartTimeMap[test.toTest().toSafeTestName()] = startTime
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
                lastRemoteFile?.let { file ->
                    lastUUID?.let {
                        val testDuration = testStartTimeMap[it.toString()].calculateTestDuration()
                        pullLastBatchVideo(file, testDuration)
                        removeRemoteVideo(file, testDuration)
                    }
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
            val testDuration = testStartTimeMap[test.toSafeTestName()].calculateTestDuration()
            if (screenRecordingPolicy == ScreenRecordingPolicy.ON_ANY || hasFailed) {
                pullTestVideo(test, testDuration)
            }
            removeRemoteVideo(device.fileManager.remoteVideoForTest(test, testBatchId), testDuration)
        } catch (e: InterruptedException) {
            logger.warn { "Can't stop recording" }
        } catch (e: TransferException) {
            logger.warn { "Can't pull video" }
        }
    }

    private suspend fun pullTestVideo(test: Test, testDuration: Long) {
        val localVideoFiles = mutableListOf<File>()
        val remoteFilePath = device.fileManager.remoteVideoForTest(test, testBatchId)
        val millis = measureTimeMillis {
            if(device.apiLevel >= 34 || videoConfiguration.timeLimit <= defaultTimeLimit || !videoConfiguration.increasedTimeLimitFeatureEnabled) {
                val localVideoFile = fileManager.createFile(FileType.VIDEO, pool, device.toDeviceInfo(), test, testBatchId)
                device.safePullFile(remoteFilePath, localVideoFile.toString())
                localVideoFiles.add(localVideoFile)
            } else {
                for (i in 0 .. (testDuration / defaultTimeLimit)) {
                    val localVideoFile = fileManager.createFile(FileType.VIDEO, pool, device.toDeviceInfo(), test, testBatchId, i.toString())
                    device.safePullFile(remoteFilePath.addFileNumberForVideo(i.toString()), localVideoFile.toString())
                    localVideoFiles.add(localVideoFile)
                }
            }
        }
        logger.trace { "Pulling finished in ${millis}ms $remoteFilePath " }
        attachmentListeners.forEach {
            localVideoFiles.forEach { localVideoFile ->
                it.onAttachment(test, Attachment(localVideoFile, AttachmentType.VIDEO))
            }
        }
    }

    /**
     * This can be called both when test times out and device unavailable
     */
    private suspend fun pullLastBatchVideo(remoteFilePath: String, testDuration: Long) {
        val millis = measureTimeMillis {
            if(device.apiLevel >= 34 || videoConfiguration.timeLimit <= defaultTimeLimit || !videoConfiguration.increasedTimeLimitFeatureEnabled) {
                val localVideoFile = fileManager.createFile(FileType.VIDEO, pool, device.toDeviceInfo(), testBatchId = testBatchId)
                device.safePullFile(remoteFilePath, localVideoFile.toString())
            } else {
                for (i in 0 .. (testDuration / defaultTimeLimit)) {
                    val localVideoFile = fileManager.createFile(FileType.VIDEO, pool, device.toDeviceInfo(), testBatchId = testBatchId, id = i.toString())
                    device.safePullFile(remoteFilePath.addFileNumberForVideo(i.toString()), localVideoFile.toString())
                }
            }
        }
        logger.trace { "Pulling finished in ${millis}ms $remoteFilePath " }
    }

    private suspend fun removeRemoteVideo(remoteFilePath: String, testDuration: Long) {
        val millis = measureTimeMillis {
            if(device.apiLevel >= 34 || videoConfiguration.timeLimit <= defaultTimeLimit || !videoConfiguration.increasedTimeLimitFeatureEnabled) {
                device.fileManager.removeRemotePath(remoteFilePath)
            } else {
                for (i in 0 .. (testDuration / defaultTimeLimit)) {
                    device.fileManager.removeRemotePath(remoteFilePath.addFileNumberForVideo(i.toString()))
                }
            }
        }
        logger.trace { "Removed file in ${millis}ms $remoteFilePath" }
    }

    private fun Long?.calculateTestDuration(): Long = this?.let { TimeUnit.SECONDS.convert(System.currentTimeMillis() - this, TimeUnit.MILLISECONDS) - 1 } ?: defaultTimeLimit // -1 in case it takes some time to start and stop recording
}

private suspend fun AndroidDevice.verifyHealthy(): Boolean {
    return withTimeoutOrNull(Duration.ofSeconds(10)) {
        executeShellCommand("echo quickhealthcheck")?.output?.let { it.contains("quickhealthcheck") } ?: false
    } ?: false
}
