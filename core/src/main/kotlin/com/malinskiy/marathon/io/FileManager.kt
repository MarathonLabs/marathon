package com.malinskiy.marathon.io

import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.test.Test
import java.io.File
import java.nio.file.Files.createDirectories
import java.nio.file.Path
import java.nio.file.Paths.get
import java.util.UUID

@Suppress("TooManyFunctions")
class FileManager(private val output: File) {
    fun createFile(fileType: FileType, pool: DevicePoolId, device: DeviceInfo, test: Test): File {
        val directory = createDirectory(fileType, pool, device)
        val filename = createFilename(test, fileType)
        return createFile(directory, filename)
    }

    fun createFile(fileType: FileType, pool: DevicePoolId, device: DeviceInfo): File {
        val directory = createDirectory(fileType, pool)
        val filename = createFilename(device, fileType)
        return createFile(directory, filename)
    }

    fun createTestResultFile(filename: String): File {
        val resultsFolder = get(output.absolutePath, FileType.TEST_RESULT.dir).toFile()
        resultsFolder.mkdirs()
        return File(resultsFolder, filename)
    }

    private fun createDirectory(fileType: FileType, pool: DevicePoolId, device: DeviceInfo): Path =
        createDirectories(getDirectory(fileType, pool, device))

    private fun createDirectory(fileType: FileType, pool: DevicePoolId): Path =
        createDirectories(getDirectory(fileType, pool))

    private fun getDirectory(fileType: FileType, pool: DevicePoolId, device: DeviceInfo): Path =
        getDirectory(fileType, pool, device.serialNumber)

    private fun getDirectory(fileType: FileType, pool: DevicePoolId, serial: String): Path =
        get(output.absolutePath, fileType.dir, pool.name, serial)

    private fun getDirectory(fileType: FileType, pool: DevicePoolId): Path =
        get(output.absolutePath, fileType.dir, pool.name)

    private fun createFile(directory: Path, filename: String): File = File(directory.toFile(), filename)

    private fun createFilename(test: Test, fileType: FileType): String = "${UUID.randomUUID()}.${fileType.suffix}"

    private fun createFilename(device: DeviceInfo, fileType: FileType): String = "${device.serialNumber}.${fileType.suffix}"
}
