package com.malinskiy.marathon.apple.ios

import com.malinskiy.marathon.apple.ios.cmd.CommandResult
import com.malinskiy.marathon.apple.ios.test.TestEvent
import com.malinskiy.marathon.apple.ios.test.TestRequest
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
}
