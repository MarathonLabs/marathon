package com.malinskiy.marathon.cache.test.serialization

import com.malinskiy.marathon.cache.CacheEntryReader
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.device.OperatingSystem
import com.malinskiy.marathon.execution.Attachment
import com.malinskiy.marathon.execution.AttachmentType
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.io.AttachmentManager
import com.malinskiy.marathon.io.FileType
import com.malinskiy.marathon.test.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.io.ByteReadChannel
import kotlinx.coroutines.io.readPacket
import kotlinx.coroutines.withContext
import kotlinx.io.streams.readerUTF8
import kotlinx.io.streams.writePacket
import java.io.File

class TestResultEntryReader(
    private val test: Test,
    private val attachmentManager: AttachmentManager
) : CacheEntryReader {

    val testResult: TestResult
        get() = _testResult

    @Volatile
    private lateinit var _testResult: TestResult

    override suspend fun readFrom(input: ByteReadChannel) {
        _testResult = TestResult(
            test = test,
            device = input.readDeviceInfo(),
            status = TestStatus.values()[input.readInt()],
            startTime = input.readLong(),
            endTime = input.readLong(),
            isFromCache = true,
            stacktrace = input.readString(),
            attachments = input.readAttachments()
        )
    }

    private suspend fun ByteReadChannel.readAttachments(): List<Attachment> {
        val attachmentCount = readInt()
        return (0 until attachmentCount).map {
            val type = AttachmentType.values()[readInt()]
            val fileType = FileType.values()[readInt()]

            attachmentManager
                .createAttachment(fileType, type)
                .also { attachment -> readFile(attachment.file) }
        }
    }

    private suspend fun ByteReadChannel.readFile(output: File) {
        val readChannel = this
        return withContext(Dispatchers.IO) {
            val fileSize = readLong()
            output
                .outputStream()
                .use {
                    it.writePacket(readChannel.readPacket(fileSize.toInt()))
                }
        }
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
