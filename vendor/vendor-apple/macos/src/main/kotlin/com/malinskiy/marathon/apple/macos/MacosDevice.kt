package com.malinskiy.marathon.apple.macos

import com.malinskiy.marathon.apple.AppleDevice
import com.malinskiy.marathon.apple.RemoteFileManager
import com.malinskiy.marathon.apple.bin.AppleBinaryEnvironment
import com.malinskiy.marathon.apple.cmd.CommandResult
import com.malinskiy.marathon.apple.model.Sdk
import com.malinskiy.marathon.apple.test.TestEvent
import com.malinskiy.marathon.apple.test.TestRequest
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.device.OperatingSystem
import com.malinskiy.marathon.device.screenshot.Rotation
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.listener.LineListener
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.ReceiveChannel
import mu.KLogger
import java.awt.image.BufferedImage
import java.io.File
import java.time.Duration

class MacosDevice : AppleDevice {
    override val udid: String
        get() = TODO("Not yet implemented")
    override val remoteFileManager: RemoteFileManager
        get() = TODO("Not yet implemented")
    override val storagePath: String
        get() = TODO("Not yet implemented")
    override val sdk: Sdk
        get() = TODO("Not yet implemented")
    override val binaryEnvironment: AppleBinaryEnvironment
        get() = TODO("Not yet implemented")

    override suspend fun setup() {
        TODO("Not yet implemented")
    }

    override suspend fun executeTestRequest(request: TestRequest): ReceiveChannel<List<TestEvent>> {
        TODO("Not yet implemented")
    }

    override suspend fun executeWorkerCommand(command: List<String>): CommandResult? {
        TODO("Not yet implemented")
    }

    override suspend fun pushFile(src: File, dst: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun pullFile(src: String, dst: File): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun pushFolder(src: File, dst: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun pullFolder(src: String, dst: File): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun install(remotePath: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getScreenshot(timeout: Duration, dst: File): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getScreenshot(timeout: Duration): BufferedImage? {
        TODO("Not yet implemented")
    }

    override suspend fun startVideoRecording(remotePath: String): CommandResult? {
        TODO("Not yet implemented")
    }

    override suspend fun stopVideoRecording(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun shutdown(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun erase(): Boolean {
        TODO("Not yet implemented")
    }

    override val operatingSystem: OperatingSystem
        get() = TODO("Not yet implemented")
    override val serialNumber: String
        get() = TODO("Not yet implemented")
    override val model: String
        get() = TODO("Not yet implemented")
    override val manufacturer: String
        get() = TODO("Not yet implemented")
    override val networkState: NetworkState
        get() = TODO("Not yet implemented")
    override val deviceFeatures: Collection<DeviceFeature>
        get() = TODO("Not yet implemented")
    override val healthy: Boolean
        get() = TODO("Not yet implemented")
    override val abi: String
        get() = TODO("Not yet implemented")
    override val logger: KLogger
        get() = TODO("Not yet implemented")

    override suspend fun prepare(configuration: Configuration) {
        TODO("Not yet implemented")
    }

    override suspend fun execute(
        configuration: Configuration,
        devicePoolId: DevicePoolId,
        testBatch: TestBatch,
        deferred: CompletableDeferred<TestBatchResults>
    ) {
        TODO("Not yet implemented")
    }

    override fun dispose() {
        TODO("Not yet implemented")
    }

    override val orientation: Rotation
        get() = TODO("Not yet implemented")

    override fun addLineListener(listener: LineListener) {
        TODO("Not yet implemented")
    }

    override fun removeLineListener(listener: LineListener) {
        TODO("Not yet implemented")
    }
}
