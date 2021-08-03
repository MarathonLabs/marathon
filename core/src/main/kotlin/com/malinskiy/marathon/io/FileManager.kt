package com.malinskiy.marathon.io

import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toTestName
import java.io.File
import java.nio.file.Files.createDirectories
import java.nio.file.Path
import java.nio.file.Paths.get

@Suppress("TooManyFunctions")
class FileManager(private val output: File) {
    val log = MarathonLogging.logger("FileManager")

    fun createFile(fileType: FileType, pool: DevicePoolId, device: DeviceInfo, test: Test, testBatchId: String? = null): File {
        val directory = createDirectory(fileType, pool, device)
        val filename = createFilename(test, fileType, testBatchId)
        return createFile(directory, filename)
    }

    fun createFile(fileType: FileType, pool: DevicePoolId, device: DeviceInfo, testBatchId: String): File {
        val directory = createDirectory(fileType, pool, device)
        val filename = createFilename(fileType, testBatchId)
        return createFile(directory, filename)
    }

    fun createFile(fileType: FileType, pool: DevicePoolId, device: DeviceInfo): File {
        val directory = createDirectory(fileType, pool)
        val filename = createFilename(device, fileType)
        return createFile(directory, filename)
    }

    fun createFolder(folderType: FolderType): File = createDirectories(get(output.absolutePath, folderType.dir)).toFile()
    fun createFolder(folderType: FolderType, pool: DevicePoolId, device: DeviceInfo): File =
        createDirectories(get(output.absolutePath, folderType.dir, pool.name, device.serialNumber)).toFile()

    fun createFolder(folderType: FolderType, pool: DevicePoolId): File =
        createDirectories(get(output.absolutePath, folderType.dir, pool.name)).toFile()

    fun createFolder(folderType: FolderType, device: DeviceInfo): File =
        createDirectories(get(output.absolutePath, folderType.dir, device.serialNumber)).toFile()

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

    private fun createFilename(fileType: FileType, testBatchId: String): String =
        "$testBatchId.${fileType.suffix}"

    private fun createFilename(test: Test, fileType: FileType, testBatchId: String? = null): String {
        val testSuffix = "${testBatchId?.let { "-$it" } ?: ""}.${fileType.suffix}"
        val testName = test.toTestName().take(256 - testSuffix.length)
        val fileName = "$testName$testSuffix"
        if(testName.length + testSuffix.length > 256) {
            log.error("File name length cannot excess 256 characters and has been trimmed to $fileName and can create a conflict. This happened because the combination of file path, test class name, and test name is too long.")
        }
        return fileName
    }

    private fun createFilename(device: DeviceInfo, fileType: FileType): String = "${device.serialNumber}.${fileType.suffix}"
}
