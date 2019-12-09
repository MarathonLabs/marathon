package com.malinskiy.marathon.cache.test.serialization

import com.malinskiy.marathon.cache.CacheEntryWriter
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.execution.Attachment
import com.malinskiy.marathon.execution.TestResult
import io.ktor.util.cio.readChannel
import kotlinx.coroutines.io.ByteWriteChannel
import kotlinx.coroutines.io.cancel
import kotlinx.coroutines.io.copyTo
import kotlinx.coroutines.io.writeBoolean
import kotlinx.io.core.buildPacket
import java.io.File

class TestResultEntryWriter(private val testResult: TestResult) : CacheEntryWriter {

    override suspend fun writeTo(output: ByteWriteChannel) {
        output.writeDeviceInfo(testResult.device)
        output.writeInt(testResult.status.ordinal)
        output.writeLong(testResult.startTime)
        output.writeLong(testResult.endTime)
        output.writeString(testResult.stacktrace)

        output.writeInt(testResult.attachments.size)
        testResult.attachments.forEach {
            output.writeAttachment(it)
        }
    }

    private suspend fun ByteWriteChannel.writeAttachment(attachment: Attachment) {
        writeInt(attachment.type.ordinal)
        writeInt(attachment.fileType.ordinal)
        writeFile(attachment.file)
    }

    private suspend fun ByteWriteChannel.writeFile(file: File) {
        val readChannel = file.readChannel()
        try {
            val fileLength = file.length()
            writeLong(file.length())
            readChannel.copyTo(this, limit = fileLength)
        } finally {
            readChannel.cancel()
        }
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
            writeInt(it.ordinal)
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
