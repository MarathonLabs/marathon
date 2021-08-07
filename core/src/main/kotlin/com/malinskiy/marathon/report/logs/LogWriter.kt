package com.malinskiy.marathon.report.logs

import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FileType
import com.malinskiy.marathon.test.Test
import java.io.File

class LogWriter(private val fileManager: FileManager) {
    fun saveLogs(test: Test, devicePoolId: DevicePoolId, testBatchId: String, device: DeviceInfo, logs: List<String>): File {
        return fileManager.createFile(FileType.LOG, devicePoolId, device, test, testBatchId = testBatchId).apply {
            writeText(logs.joinToString("\n"))
        }
    }

    fun appendLogs(devicePoolId: DevicePoolId, device: DeviceInfo, log: String) {
        val logFile = fileManager.createFile(FileType.LOG, devicePoolId, device)
        logFile.appendText("$log\n")
    }
}
