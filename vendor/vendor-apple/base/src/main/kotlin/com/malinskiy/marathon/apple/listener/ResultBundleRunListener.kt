package com.malinskiy.marathon.apple.listener

import com.malinskiy.marathon.apple.AppleDevice
import com.malinskiy.marathon.apple.RemoteFileManager
import com.malinskiy.marathon.config.vendor.apple.ios.XcresultConfiguration
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FolderType
import com.malinskiy.marathon.log.MarathonLogging
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
    override suspend fun afterTestRun() {
        super.afterTestRun()
        val remotePath = device.remoteFileManager.remoteXcresultFile(batch)
        if (xcresultConfiguration.pull) {
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
}
