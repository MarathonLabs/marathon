package com.malinskiy.marathon.apple.listener

import com.malinskiy.marathon.apple.AppleDevice
import com.malinskiy.marathon.apple.RemoteFileManager
import com.malinskiy.marathon.apple.logparser.parser.DeviceFailureReason
import com.malinskiy.marathon.config.vendor.apple.ios.PullingPolicy
import com.malinskiy.marathon.config.vendor.apple.ios.XcresultConfiguration
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FolderType
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import java.io.File

class ResultBundleRunListener(
    private val device: AppleDevice,
    private val xcresultConfiguration: XcresultConfiguration,
    private val poolId: DevicePoolId,
    private val batch: TestBatch,
    private val fileManager: FileManager,
) : AppleTestRunListener {

    private val logger = MarathonLogging.logger {}

    private var isBatchFailed = false

    override suspend fun testRunFailed(errorMessage: String, reason: DeviceFailureReason) {
        super.testRunFailed(errorMessage, reason)
        isBatchFailed = true
    }

    override suspend fun testFailed(test: Test, startTime: Long, endTime: Long, trace: String?) {
        super.testFailed(test, startTime, endTime, trace)
        isBatchFailed = true
    }

    override suspend fun afterTestRun() {
        super.afterTestRun()
        val remotePath = device.remoteFileManager.remoteXcresultFile(batch)
        if (isXcresultPullNeeded()) {
            val localPath = File(fileManager.createFolder(FolderType.DEVICE_FILES, poolId, device = device.toDeviceInfo()), "xcresult").apply { mkdirs() }
            if (!device.pullFolder(remotePath, localPath)) {
                logger.warn { "failed to pull result bundle" }
            } else {
                File(localPath, remotePath.substringAfterLast(RemoteFileManager.FILE_SEPARATOR))
                    .renameTo(File(localPath, "${batch.id}.xcresult"))
            }
        }
        if (xcresultConfiguration.remoteClean) {
            device.remoteFileManager.removeRemotePath(remotePath)
        }
    }

    private fun isXcresultPullNeeded() : Boolean {
        return when (xcresultConfiguration.pullingPolicy) {
            PullingPolicy.ALWAYS -> true
            PullingPolicy.NEVER -> false
            PullingPolicy.ON_FAILURE -> isBatchFailed
        }
    }
}
