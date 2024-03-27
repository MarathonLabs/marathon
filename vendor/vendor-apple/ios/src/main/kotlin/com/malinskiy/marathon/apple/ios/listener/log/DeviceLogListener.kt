package com.malinskiy.marathon.apple.ios.listener.log

import com.malinskiy.marathon.apple.extensions.Durations
import com.malinskiy.marathon.apple.ios.AppleSimulatorDevice
import com.malinskiy.marathon.apple.listener.AppleTestRunListener
import com.malinskiy.marathon.apple.model.Sdk
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.io.FileType
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.TestBatch
import kotlin.system.measureTimeMillis

class DeviceLogListener(
    private val device: AppleSimulatorDevice,
    private val enabled: Boolean,
    private val pool: DevicePoolId,
    private val testBatch: TestBatch,
) : AppleTestRunListener {

    private var pid: String? = null
    private val logger = MarathonLogging.logger {}
    private val supported by lazy {
        when (device.sdk) {
            Sdk.IPHONESIMULATOR, Sdk.TV_SIMULATOR, Sdk.WATCH_SIMULATOR, Sdk.VISION_SIMULATOR -> {
                true
            }

            else -> false
        }
    }

    override suspend fun beforeTestRun() {
        super.beforeTestRun()
        if (!enabled) return

        if (!supported) {
            logger.warn { "Device ${device.serialNumber} does not support capturing device logs" }
            return
        }

        val remoteLogPath = device.remoteFileManager.remoteLog()
        val result =
            device.commandExecutor.criticalExecute(
                Durations.INFINITE,
                "sh",
                "-c",
                "xcrun simctl spawn ${device.udid} log stream --type log --color none --style compact 2>/dev/null > $remoteLogPath & echo $!"
            )
        val possiblePid = result.combinedStdout.trim()
        if (result.successful && possiblePid.toIntOrNull() != null) {
            pid = result.combinedStdout.trim()
        }
    }

    override suspend fun afterTestRun() {
        super.afterTestRun()
        if (!enabled) return
        if (!supported) {
            logger.warn { "Device ${device.serialNumber} does not support capturing device logs" }
            return
        }

        if (pid != null) {
            device.executeWorkerCommand(listOf("sh", "-c", "kill -s INT $pid"))
            pullLogfile()
        }
    }

    private suspend fun pullLogfile() {
        val localLogFile =
            device.fileManager.createFile(FileType.DEVICE_LOG, pool, device.toDeviceInfo(), test = null, testBatchId = testBatch.id)
        val remoteFilePath = device.remoteFileManager.remoteLog()
        val millis = measureTimeMillis {
            device.pullFile(remoteFilePath, localLogFile)
        }
        logger.debug { "Pulling finished in ${millis}ms $remoteFilePath " }
    }
}
