package com.malinskiy.marathon.cache.test.serialization

import com.malinskiy.marathon.cache.CacheEntryReader
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.device.OperatingSystem
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.test.Test
import kotlinx.coroutines.io.ByteReadChannel
import kotlinx.coroutines.io.readPacket
import kotlinx.io.streams.readerUTF8

class TestResultEntryReader(private val test: Test) : CacheEntryReader {

    val testResult: TestResult
        get() = _testResult

    private lateinit var _testResult: TestResult

    override suspend fun readFrom(input: ByteReadChannel) {
        _testResult = TestResult(
            test = test,
            device = input.readDeviceInfo(),
            status = TestStatus.values()[input.readInt()],
            startTime = input.readLong(),
            endTime = input.readLong(),
            stacktrace = input.readString(),
            attachments = emptyList()
        )

        // TODO: deserialize attachments
    }

    private suspend fun ByteReadChannel.readDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            operatingSystem = OperatingSystem(readStringNonNull()),
            serialNumber = readStringNonNull(),
            model = readStringNonNull(),
            manufacturer = readStringNonNull(),
            networkState = NetworkState.values()[readInt()],
            deviceFeatures = readDeviceFeatures(),
            healthy = readBoolean()
        )
    }

    private suspend fun ByteReadChannel.readString(): String? {
        val isNull = !readBoolean()
        if (isNull) return null

        val packetSize = readLong()
        val packet = readPacket(packetSize.toInt())
        return packet.readerUTF8().use { it.readText() }
    }

    private suspend fun ByteReadChannel.readStringNonNull(): String = readString()!!

    private suspend inline fun ByteReadChannel.readDeviceFeatures(): Collection<DeviceFeature> =
        (0 until readInt()).map { DeviceFeature.values()[readInt()] }

}
