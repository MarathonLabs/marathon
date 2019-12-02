package com.malinskiy.marathon.io

import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.Attachment
import com.malinskiy.marathon.execution.AttachmentType
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toTestName
import java.io.File
import java.nio.file.Files.createDirectories
import java.nio.file.Path
import java.nio.file.Paths.get

class AttachmentManager(private val outputDirectory: File) {

    private val tempFiles: MutableList<File> = arrayListOf()

    fun createAttachment(fileType: FileType, attachmentType: AttachmentType): Attachment {
        val file = File
            .createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX)
            .apply { deleteOnExit() }

        return Attachment(
            file = file,
            type = attachmentType,
            fileType = fileType
        )
    }

    fun writeToTarget(
        poolId: DevicePoolId,
        device: DeviceInfo,
        test: Test,
        attachment: Attachment
    ) {
        val directory = createDirectory(attachment.fileType, poolId, device)
        val filename = createFilename(test, attachment.fileType)
        val targetFile = createFile(directory, filename)

        attachment.file.copyTo(targetFile)
    }

    fun terminate() {
        tempFiles.forEach { it.delete() }
    }

    private fun createDirectory(fileType: FileType, pool: DevicePoolId, device: DeviceInfo): Path =
        createDirectories(getDirectory(fileType, pool, device))

    private fun getDirectory(fileType: FileType, pool: DevicePoolId, device: DeviceInfo): Path =
        getDirectory(fileType, pool, device.serialNumber)

    private fun getDirectory(fileType: FileType, pool: DevicePoolId, serial: String): Path =
        get(outputDirectory.absolutePath, fileType.dir, pool.name, serial)

    private fun createFile(directory: Path, filename: String): File = File(directory.toFile(), filename)

    private fun createFilename(test: Test, fileType: FileType): String = "${test.toTestName()}.${fileType.suffix}"

    private companion object {
        private const val TEMP_FILE_PREFIX = "test_run_attachment"
        private const val TEMP_FILE_SUFFIX = "tmp"
    }
}
