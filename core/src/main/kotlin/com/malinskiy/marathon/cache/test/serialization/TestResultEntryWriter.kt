package com.malinskiy.marathon.cache.test.serialization

import com.malinskiy.marathon.cache.CacheEntryWriter
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.execution.TestResult
import kotlinx.coroutines.io.ByteWriteChannel
import kotlinx.coroutines.io.writeBoolean
import kotlinx.io.core.buildPacket

class TestResultEntryWriter(private val testResult: TestResult) : CacheEntryWriter {

    override suspend fun writeTo(output: ByteWriteChannel) {
        output.writeDeviceInfo(testResult.device)
        output.writeInt(testResult.status.ordinal)
        output.writeLong(testResult.startTime)
        output.writeLong(testResult.endTime)
        output.writeString(testResult.stacktrace)

        // TODO: serialize attachments
    }

    private suspend fun ByteWriteChannel.writeDeviceInfo(deviceInfo: DeviceInfo) {
        writeString(deviceInfo.operatingSystem.version)
        writeString(deviceInfo.serialNumber)
        writeString(deviceInfo.model)
        writeString(deviceInfo.manufacturer)
        writeInt(deviceInfo.networkState.ordinal)
        writeEnumCollection(deviceInfo.deviceFeatures)
        writeBoolean(deviceInfo.healthy)
    }

    private suspend fun ByteWriteChannel.writeEnumCollection(collection: Collection<Enum<*>>) {
        writeInt(collection.size)
        collection.forEach {
            it.ordinal
        }
    }

    private suspend fun ByteWriteChannel.writeString(str: String?) {
        if (str == null) {
            writeBoolean(false)
            return
        }

        writeBoolean(true)

        val packet = buildPacket {
            writeStringUtf8(str)
        }

        writeLong(packet.remaining)
        writePacket(packet)
    }
}
