package com.malinskiy.marathon.ios.executor.listener.video

import com.malinskiy.marathon.config.ScreenRecordingPolicy
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.exceptions.TransferException
import com.malinskiy.marathon.execution.Attachment
import com.malinskiy.marathon.execution.AttachmentType
import com.malinskiy.marathon.extension.withTimeoutOrNull
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FileType
import com.malinskiy.marathon.ios.AppleDevice
import com.malinskiy.marathon.ios.AppleSimulatorDevice
import com.malinskiy.marathon.ios.RemoteFileManager
import com.malinskiy.marathon.ios.executor.listener.AppleTestRunListener
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureReason
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.attachment.AttachmentProvider
import com.malinskiy.marathon.report.attachment.AttachmentProviderDelegate
import com.malinskiy.marathon.test.Test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.isActive
import java.time.Duration
import kotlin.system.measureTimeMillis

class ScreenRecordingListener(
    private val fileManager: FileManager,
    private val remoteFileManager: RemoteFileManager,
    private val pool: DevicePoolId,
    private val testBatchId: String,
    private val device: AppleSimulatorDevice,
    private val screenRecordingPolicy: ScreenRecordingPolicy,
    coroutineScope: CoroutineScope,
    private val attachmentProvider: AttachmentProviderDelegate = AttachmentProviderDelegate(),
) : AppleTestRunListener, AttachmentProvider by attachmentProvider, CoroutineScope by coroutineScope {
    private val logger = MarathonLogging.logger {}

    private var supervisorJob: Job? = null
    private var lastRemoteFile: String? = null
    private var started: Boolean = false

    override suspend fun beforeTestRun() {
        super.beforeTestRun()
        stop()
    }

    override suspend fun testStarted(test: Test) {
        val remoteFile = remoteFileManager.remoteVideoForTest(test, testBatchId)
        lastRemoteFile = remoteFile
        val supervisor = SupervisorJob()
        supervisorJob = supervisor
        async(supervisor) {
            val existingPidfile = device.remoteFileManager.remoteVideoPidfile()
            var informedUser = false
            while(isActive) {
                /**
                 * We wait for the previous process to die, otherwise we risk overloading the worker with many encoding processes
                 * that all encode the same frames
                 */
                if(device.readTextfile(existingPidfile) == null) {
                    //Previous recording finished
                    break
                } else if(!informedUser) {
                    informedUser = true
                    logger.warn { "Previous video recording didn't finish yet. Current recording might be missing or cut from the start" }
                }
            }
            val startResult = device.startVideoRecording(remoteFile)
            started = startResult?.exitCode == 0
            if (started) {
                val existingPidfile = device.remoteFileManager.remoteVideoPidfile()
                val pid = device.readTextfile(existingPidfile)?.trim()
                logger.debug { "started video recording pid=$pid to $remoteFile" }
            }
        }
    }

    override suspend fun testFailed(test: Test, startTime: Long, endTime: Long) {
        if (started) {
            stop()
        }
        lastRemoteFile = null
        pullVideo(test, false)
    }

    private suspend fun pullVideo(test: Test, success: Boolean) {
        try {
            if (screenRecordingPolicy == ScreenRecordingPolicy.ON_ANY ||
                screenRecordingPolicy == ScreenRecordingPolicy.ON_FAILURE && !success
            ) {
                pullTestVideo(test)
            }
            removeRemoteVideo(remoteFileManager.remoteVideoForTest(test, testBatchId))
        } catch (e: TransferException) {
            logger.warn { "Can't pull video" }
        }
    }

    override suspend fun testPassed(test: Test, startTime: Long, endTime: Long) {
        if (started) {
            stop()
        }
        lastRemoteFile = null
        pullVideo(test, true)
    }

    override suspend fun testRunFailed(errorMessage: String, reason: DeviceFailureReason) {
        try {
            if (device.verifyHealthy()) {
                stop()
                lastRemoteFile?.let {
                    pullLastBatchVideo(it)
                    removeRemoteVideo(it)
                }
            }
        } catch (e: TransferException) {
            logger.warn { "Can't pull video" }
        }
    }

    override suspend fun afterTestRun() {
        stop()
    }

    /**
     * Safe to execute multiple times
     * Trading off one request to really detect if we're running anything
     * Synchronizes the state via pidfile with recorder pid in it
     * Pidfile is removed after the recording process is stopped
     */
    private suspend fun stop() {
        val existingPidfile = device.remoteFileManager.remoteVideoPidfile()
        val pid = device.readTextfile(existingPidfile)?.trim()
        if (!pid.isNullOrBlank()) {
            val success = device.stopVideoRecording()
            logger.debug { "stopped video recording pid=$pid" }
        }
        supervisorJob?.cancelAndJoin()
    }

    private suspend fun pullTestVideo(test: Test) {
        val localVideoFile = fileManager.createFile(FileType.VIDEO, pool, device.toDeviceInfo(), test, testBatchId)
        val remoteFilePath = remoteFileManager.remoteVideoForTest(test, testBatchId)
        val millis = measureTimeMillis {
            device.pullFile(remoteFilePath, localVideoFile)
        }
        logger.trace { "Pulling finished in ${millis}ms $remoteFilePath " }
        attachmentProvider.onAttachment(test, Attachment(localVideoFile, AttachmentType.VIDEO))
    }

    private suspend fun pullLastBatchVideo(remoteFilePath: String) {
        val localVideoFile = fileManager.createFile(FileType.VIDEO, pool, device.toDeviceInfo(), testBatchId)
        val millis = measureTimeMillis {
            device.pullFile(remoteFilePath, localVideoFile)
        }
        logger.trace { "Pulling finished in ${millis}ms $remoteFilePath " }
    }

    private suspend fun removeRemoteVideo(remoteFilePath: String) {
        val millis = measureTimeMillis {
            remoteFileManager.removeRemotePath(remoteFilePath)
        }
        logger.trace { "Removed file in ${millis}ms $remoteFilePath" }
    }
}

private suspend fun AppleDevice.verifyHealthy(): Boolean {
    return withTimeoutOrNull(Duration.ofSeconds(10)) {
        try {
            executeWorkerCommand(listOf("echo", "quickhealthcheck"))?.combinedStdout?.contains("quickhealthcheck")
        } catch (e: Exception) {
            null
        }
    } ?: false
}
