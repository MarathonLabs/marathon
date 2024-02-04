package com.malinskiy.marathon.io

import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.extension.escape
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toTestName
import java.io.File
import java.nio.file.Files.createDirectories
import java.nio.file.Path
import java.nio.file.Paths.get
import java.util.UUID

/**
 * Validation logic should check filename first, then check if the resulting path is within max path len
 */
@Suppress("TooManyFunctions")
class FileManager(private val maxPath: Int, private val maxFilename: Int, private val output: File) {
    val log = MarathonLogging.logger("FileManager")

    fun createFile(
        fileType: FileType,
        pool: DevicePoolId,
        device: DeviceInfo,
        test: Test? = null,
        testBatchId: String? = null,
        id: String? = null
    ): File {
        val directory = when {
            test != null || testBatchId != null -> createDirectory(fileType, pool, device)
            else -> createDirectory(fileType, pool)
        }
        val filename = when {
            test != null -> createTestFilename(test, fileType, testBatchId, id = id)
            testBatchId != null -> createBatchFilename(testBatchId, fileType, id = id)
            else -> createDeviceFilename(device, fileType, id = id)
        }
        return createFile(directory, filename)
    }

    fun createFolder(folderType: FolderType, pool: DevicePoolId? = null, device: DeviceInfo? = null): File {
        var path = get(output.absolutePath, folderType.dir)
        if (pool != null) {
            path = path.resolve(pool.name)
        }
        if (device != null) {
            path = path.resolve(device.safeSerialNumber)
        }

        val maybeTooLongPath = path.toFile()
        path = if (maxPath > 0 && maybeTooLongPath.absolutePath.length > maxPath) {
            val trimmed = maybeTooLongPath.absolutePath.take(maxPath)
            log.error {
                "Directory path length cannot exceed $maxPath characters and has been trimmed from $maybeTooLongPath to $trimmed and can create a conflict. " +
                    "This happened because the combination of file path, pool name and device serial is too long."
            }
            File(trimmed)
        } else {
            maybeTooLongPath
        }.toPath()

        return createDirectories(path).toFile()
    }

    fun createTestResultFile(filename: String): File {
        val resultsFolder = get(output.absolutePath, FileType.TEST_RESULT.dir)
        resultsFolder.toFile().mkdirs()
        return createFile(resultsFolder, filename)
    }

    private fun createDirectory(fileType: FileType, pool: DevicePoolId, device: DeviceInfo? = null): Path {
        return createDirectories(getDirectory(fileType, pool, serial = device?.safeSerialNumber))
    }

    private fun getDirectory(fileType: FileType, pool: DevicePoolId, serial: String? = null): Path {
        val path = get(output.absolutePath, fileType.dir, pool.name)
        return serial?.let {
            path.resolve(serial)
        } ?: path
    }

    private fun createFile(directory: Path, filename: String): File {
        val trimmedFilename = if (maxFilename > 0 && filename.length > maxFilename) {
            val safeFilename = filename.take(maxFilename)
            log.error {
                "File name length cannot exceed $maxFilename characters and has been trimmed to $safeFilename and can create a conflict." +
                    "This usually happens because the test name is too long."
            }
            safeFilename
        } else {
            filename
        }
        val maybeTooLongPath = File(directory.toFile(), trimmedFilename)
        return if (maxPath > 0 && maybeTooLongPath.absolutePath.length > maxPath) {
            val trimmed = maybeTooLongPath.absolutePath.substring(0 until maxPath)
            log.error {
                "File path length cannot exceed $maxPath characters and has been trimmed from $maybeTooLongPath to $trimmed and can create a conflict. " +
                    "This happened because the combination of file path, test class name, and test name is too long."
            }
            File(trimmed)
        } else {
            maybeTooLongPath
        }
    }

    private fun createBatchFilename(testBatchId: String, fileType: FileType, id: String? = null): String {
        return StringBuilder().apply {
            append(testBatchId)
            if (id != null) {
                append("-$id")
            }
            if (fileType.suffix.isNotEmpty()) {
                append(".$testBatchId")
            }
        }.toString()
    }

    private fun createTestFilename(
        test: Test,
        fileType: FileType,
        testBatchId: String? = null,
        overrideExtension: String? = null,
        id: String? = null,
    ): String {
        val testSuffix = StringBuilder().apply {
            if (testBatchId != null) {
                append("-$testBatchId")
            }
            if (id != null) {
                append("-$id")
            }
            if (overrideExtension != null) {
                append(".${overrideExtension}")
            } else if (fileType.suffix.isNotEmpty()) {
                append(".${fileType.suffix}")
            }
        }.toString()
        val testName = test.toTestName().escape()
        return "$testName$testSuffix"
    }

    private fun createDeviceFilename(device: DeviceInfo, fileType: FileType, id: String? = null): String {
        return StringBuilder().apply {
            append(device.safeSerialNumber)
            if (id != null) {
                append("-$id")
            }
            if (fileType.suffix.isNotEmpty()) {
                append(".${fileType.suffix}")
            }
        }.toString()
    }
}
