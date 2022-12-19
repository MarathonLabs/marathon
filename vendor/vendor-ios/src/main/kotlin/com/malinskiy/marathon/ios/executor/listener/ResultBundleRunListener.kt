package com.malinskiy.marathon.ios.executor.listener

import com.malinskiy.marathon.config.vendor.ios.XcresultConfiguration
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FolderType
import com.malinskiy.marathon.ios.AppleDevice
import com.malinskiy.marathon.ios.RemoteFileManager
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.TestBatch

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
            val localPath = fileManager.createFolder(FolderType.DEVICE_FILES, poolId, device.toDeviceInfo())
            if (!device.pullFolder(remotePath, localPath)) {
                logger.warn { "failed to pull result bundle" }
            }
        }
        if (xcresultConfiguration.remoteClean) {
            device.remoteFileManager.removeRemotePath(remotePath)
        }
    }
}
