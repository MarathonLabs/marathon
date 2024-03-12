package com.malinskiy.marathon.apple

import com.malinskiy.marathon.apple.bin.AppleBinaryEnvironment
import com.malinskiy.marathon.apple.cmd.CommandResult
import com.malinskiy.marathon.apple.configuration.Transport
import com.malinskiy.marathon.apple.model.Arch
import com.malinskiy.marathon.apple.model.Sdk
import com.malinskiy.marathon.apple.test.TestEvent
import com.malinskiy.marathon.apple.test.TestRequest
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.screenshot.Screenshottable
import com.malinskiy.marathon.exceptions.DeviceSetupException
import com.malinskiy.marathon.report.logs.LogProducer
import kotlinx.coroutines.channels.ReceiveChannel
import java.io.File
import java.time.Duration

interface AppleDevice : Device, Screenshottable, LogProducer {
    val udid: String
    val remoteFileManager: RemoteFileManager
    val storagePath: String
    val transport: Transport

    val arch: Arch
        get() = when {
            sdk == Sdk.IPHONESIMULATOR -> {
                when (abi) {
                    "x86_64" -> Arch.x86_64
                    "arm64" -> Arch.arm64
                    else -> Arch.arm64
                }
            }

            udid.contains('-') -> Arch.arm64e
            else -> Arch.arm64
        }
    val sdk: Sdk
    val binaryEnvironment: AppleBinaryEnvironment
    
    /**
     * Called only once per device's lifetime
     * 
     * @throws DeviceSetupException if something went wrong
     */
    suspend fun setup()

    suspend fun executeTestRequest(request: TestRequest): ReceiveChannel<List<TestEvent>>

    suspend fun executeWorkerCommand(command: List<String>): CommandResult?

    suspend fun pushFile(src: File, dst: String): Boolean
    suspend fun pullFile(src: String, dst: File): Boolean
    
    suspend fun pushFolder(src: File, dst: String): Boolean
    suspend fun pullFolder(src: String, dst: File): Boolean
    
    suspend fun install(remotePath: String): Boolean
    
    suspend fun getScreenshot(timeout: Duration, dst: File): Boolean
    suspend fun startVideoRecording(remotePath: String): CommandResult?
    suspend fun stopVideoRecording(): Boolean
    suspend fun shutdown(): Boolean
    suspend fun erase(): Boolean

    companion object {
        const val SHARED_PATH = "/tmp/marathon"
    }
}
