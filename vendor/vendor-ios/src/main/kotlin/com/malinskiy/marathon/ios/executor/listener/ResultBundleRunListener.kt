package com.malinskiy.marathon.ios.executor.listener

import com.malinskiy.marathon.config.vendor.ios.XcresultConfiguration
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FolderType
import com.malinskiy.marathon.ios.DerivedDataManager
import com.malinskiy.marathon.ios.IOSDevice
import com.malinskiy.marathon.ios.RemoteFileManager
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.TestBatch

class ResultBundleRunListener(
    private val device: IOSDevice,
    private val xcresultConfiguration: XcresultConfiguration,
    private val poolId: DevicePoolId,
    private val batch: TestBatch,
    private val fileManager: FileManager,
    private val derivedDataManager: DerivedDataManager
) : IOSTestRunListener {

    private val logger = MarathonLogging.logger {}
    override suspend fun afterTestRun() {
        super.afterTestRun()
        val remotePath = RemoteFileManager.remoteXcresultFile(device, batch)
        if (xcresultConfiguration.pull) {
            val localPath = fileManager.createFolder(FolderType.DEVICE_FILES, poolId, device.toDeviceInfo())
            val exitCode = derivedDataManager.receive(
                remotePath.path,
                device.hostCommandExecutor.hostAddress.hostName,
                device.hostCommandExecutor.port,
                localPath
            )
            if (exitCode != 0) {
                logger.warn { "failed to pull remote bundle" }
            }
        }
        if (xcresultConfiguration.remoteClean) {
            RemoteFileManager.removeRemotePath(device, remotePath.path)
        }
    }
}
