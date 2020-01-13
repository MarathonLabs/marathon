package com.malinskiy.marathon.android.executor.listeners.video

import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.exception.TransferException
import com.malinskiy.marathon.android.executor.listeners.NoOpTestRunListener
import com.malinskiy.marathon.android.executor.listeners.line.LineListener
import com.malinskiy.marathon.android.executor.listeners.line.NullOutputListener
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
import com.malinskiy.marathon.test.Test
import kotlin.system.measureTimeMillis

const val MS_IN_SECOND: Long = 1_000L

class ScreenRecorderTestRunListener(
    private val fileManager: FileManager,
    private val pool: DevicePoolId,
    private val device: AndroidDevice
) : NoOpTestRunListener(), AttachmentProvider {

    val attachmentListeners = mutableListOf<AttachmentListener>()

    override fun registerListener(listener: AttachmentListener) {
        attachmentListeners.add(listener)
    }

    private val logger = MarathonLogging.logger("ScreenRecorder")

    private val screenRecorderStopper = ScreenRecorderStopper(device)

    private var hasFailed: Boolean = false
    private var recorder: Thread? = null
    private var outputListener: LineListener? = null

    private val awaitMillis = MS_IN_SECOND

    override fun testStarted(test: TestIdentifier) {
        hasFailed = false

        outputListener = NullOutputListener()

        val screenRecorder = ScreenRecorder(device, outputListener!!, device.fileManager.remoteVideoForTest(test.toTest()))
        recorder = kotlin.concurrent.thread {
            screenRecorder.run()
        }
    }

    override fun testFailed(test: TestIdentifier, trace: String) {
        hasFailed = true
    }

    override fun testAssumptionFailure(test: TestIdentifier, trace: String) {
        pullVideo(test.toTest())
    }

    override fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {
        pullVideo(test.toTest())
    }

    private fun pullVideo(test: Test) {
        try {
            val join = measureTimeMillis {
                recorder?.join(awaitMillis)
            }
            logger.trace { "join ${join}ms" }
            if (hasFailed) {
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

    private fun pullTestVideo(test: Test) {
        val localVideoFile = fileManager.createFile(FileType.VIDEO, pool, device.toDeviceInfo(), test)
        val remoteFilePath = device.fileManager.remoteVideoForTest(test)
        val millis = measureTimeMillis {
            device.fileManager.pullFile(remoteFilePath, localVideoFile.toString())
        }
        logger.trace { "Pulling finished in ${millis}ms $remoteFilePath " }
        attachmentListeners.forEach { it.onAttachment(test, Attachment(localVideoFile, AttachmentType.VIDEO)) }
    }

    private fun removeTestVideo(test: Test) {
        val remoteFilePath = device.fileManager.remoteVideoForTest(test)
        val millis = measureTimeMillis {
            device.fileManager.removeRemotePath(remoteFilePath)
        }
        logger.trace { "Removed file in ${millis}ms $remoteFilePath" }
    }
}
