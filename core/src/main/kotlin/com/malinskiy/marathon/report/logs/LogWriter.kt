package com.malinskiy.marathon.report.logs

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FileType
import com.malinskiy.marathon.test.Test
import java.io.File

class LogWriter(private val fileManager: FileManager) {
    fun saveLogs(test: Test, devicePoolId: DevicePoolId, device: Device, logs: List<String>): File {
        return fileManager.createFile(FileType.LOG, devicePoolId, device, test).apply {
            writeText(logs.joinToString("\n"))
        }
    }

    fun appendLogs(test: Test, devicePoolId: DevicePoolId, device: Device, log: String) {
        val logFile = fileManager.createFile(FileType.LOG, devicePoolId, device, test)
        logFile.appendText("$log\n")
    }
}
