package com.malinskiy.marathon.apple.ios.listener.log

import com.malinskiy.marathon.apple.extensions.Durations
import com.malinskiy.marathon.apple.ios.AppleSimulatorDevice
import com.malinskiy.marathon.apple.listener.AppleTestRunListener
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.io.FileType
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.TestBatch
import kotlin.system.measureTimeMillis

class SimulatorLogListener(
    private val device: AppleSimulatorDevice,
    private val pool: DevicePoolId,
    private val testBatch: TestBatch,
) : AppleTestRunListener {

    private var pid: String? = null
    private val logger = MarathonLogging.logger {}

    override suspend fun beforeTestRun() {
        super.beforeTestRun()
        val remoteLogPath = device.remoteFileManager.remoteLog()
        val result =
            device.commandExecutor.criticalExecute(Durations.INFINITE, "sh", "-c", "xcrun simctl spawn ${device.udid} log stream --type log --color none --style compact 2>/dev/null > $remoteLogPath & echo $!")
        val possiblePid = result.combinedStdout.trim()
        if (result.successful && possiblePid.toIntOrNull() != null) {
            pid = result.combinedStdout.trim()
        }
    }

    override suspend fun afterTestRun() {
        super.afterTestRun()
        if (pid != null) {
            device.executeWorkerCommand(listOf("sh", "-c", "kill -s INT $pid"))
            pullLogfile()
        }
    }

    private suspend fun pullLogfile() {
        val localVideoFile =
            device.fileManager.createFile(FileType.DEVICE_LOG, pool, device.toDeviceInfo(), test = null, testBatchId = testBatch.id)
        val remoteFilePath = device.remoteFileManager.remoteLog()
        val millis = measureTimeMillis {
            device.pullFile(remoteFilePath, localVideoFile)
        }
        logger.debug { "Pulling finished in ${millis}ms $remoteFilePath " }
    }
}
