package com.malinskiy.marathon.android.executor.listeners

import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.model.TestIdentifier
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FileType
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.TestBatch

class CodeCoverageListener(
    private val device: AndroidDevice,
    private val devicePoolId: DevicePoolId,
    private val fileManager: FileManager,
    private val testBatch: TestBatch
) : NoOpTestRunListener() {
    private val logger = MarathonLogging.logger("CodeCoverageListener")

    override suspend fun testRunEnded(elapsedTime: Long, runMetrics: Map<String, String>) {
        val src = "${device.externalStorageMount}/coverage-${testBatch.id}.ec"
        val dest = fileManager.createFile(FileType.COVERAGE, devicePoolId, device.toDeviceInfo(), testBatch.id).absolutePath

        try {
            device.pullFile(src, dest)
        } catch (e: Exception) {
            logger.error("Downloading coverage file $src failed!", e)
        }
    }
}
