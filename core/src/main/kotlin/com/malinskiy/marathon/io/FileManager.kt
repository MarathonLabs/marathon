package com.malinskiy.marathon.io

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toTestName
import java.io.File
import java.nio.file.Files.createDirectories
import java.nio.file.Path
import java.nio.file.Paths.get

class FileManager(private val output: File) {
    fun createFile(fileType: FileType, pool: DevicePoolId, device: Device, test: Test): File {
        val directory = createDirectory(fileType, pool, device)
        val filename = createFilename(test, fileType)
        return createFile(directory, filename)
    }

    private fun createDirectory(test: FileType, pool: DevicePoolId, device: Device): Path =
            createDirectories(getDirectory(test, pool, device))

    private fun getDirectory(fileType: FileType, pool: DevicePoolId, device: Device): Path =
            get(output.absolutePath, fileType.dir, pool.name, device.serialNumber)

    private fun createFile(directory: Path, filename: String): File = File(directory.toFile(), filename)

    private fun createFilename(test: Test, fileType: FileType): String = "${test.toTestName()}.${fileType.suffix}"
}
