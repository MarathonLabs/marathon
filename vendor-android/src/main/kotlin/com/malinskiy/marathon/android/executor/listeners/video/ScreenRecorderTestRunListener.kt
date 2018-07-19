package com.malinskiy.marathon.android.executor.listeners.video

import com.android.ddmlib.CollectingOutputReceiver
import com.android.ddmlib.IDevice
import com.android.ddmlib.SyncException
import com.android.ddmlib.testrunner.TestIdentifier
import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.RemoteFileManager
import com.malinskiy.marathon.android.RemoteFileManager.removeRemotePath
import com.malinskiy.marathon.android.executor.listeners.NoOpTestRunListener
import com.malinskiy.marathon.android.toTest
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FileType
import mu.KotlinLogging
import kotlin.system.measureTimeMillis

internal class ScreenRecorderTestRunListener(private val fileManager: FileManager,
                                             private val pool: DevicePoolId,
                                             private val device: AndroidDevice) : NoOpTestRunListener() {
    private val logger = KotlinLogging.logger("ScreenRecorder")

    private val deviceInterface: IDevice = device.ddmsDevice

    private var hasFailed: Boolean = false
    private var thread: Thread? = null
    private var receiver: CollectingOutputReceiver? = null

    private val awaitMillis = 1_000L

    override fun testStarted(test: TestIdentifier) {
        hasFailed = false

        receiver = CollectingOutputReceiver()
        val screenRecorder = ScreenRecorder(deviceInterface, test, receiver!!)
        thread = Thread(screenRecorder, "ScreenRecorder").also { it.start() }
    }

    override fun testFailed(test: TestIdentifier, trace: String) {
        hasFailed = true
    }

    override fun testAssumptionFailure(test: TestIdentifier, trace: String) {
        receiver!!.cancel()
        pullVideo(test)
    }

    override fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {
        receiver!!.cancel()
        pullVideo(test)
    }

    private fun pullVideo(test: TestIdentifier) {
        try {
            val join = measureTimeMillis {
                thread?.join(awaitMillis)
            }
            logger.trace { "join ${join}ms" }
            if (hasFailed) {
                pullTestVideo(test)
            }
            removeTestVideo(test)
        } catch (e: InterruptedException) {
            logger.warn { "Can't stop recording" }
        } catch (e: SyncException) {
            logger.warn { "Can't pull video" }
        }
    }

    private fun pullTestVideo(test: TestIdentifier) {
        val localVideoFile = fileManager.createFile(FileType.VIDEO, pool, device, test.toTest())
        val remoteFilePath = RemoteFileManager.remoteVideoForTest(test)
        val millis = measureTimeMillis {
            deviceInterface.pullFile(remoteFilePath, localVideoFile.toString())
        }
        logger.trace { "Pulling finished in ${millis}ms $remoteFilePath " }
    }

    private fun removeTestVideo(test: TestIdentifier) {
        val remoteFilePath = RemoteFileManager.remoteVideoForTest(test)
        val millis = measureTimeMillis {
            removeRemotePath(deviceInterface, remoteFilePath)
        }
        logger.trace { "Removed file in ${millis}ms $remoteFilePath" }
    }
}
