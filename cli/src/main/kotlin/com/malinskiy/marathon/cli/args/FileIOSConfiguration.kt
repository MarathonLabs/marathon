package com.malinskiy.marathon.cli.args

import com.fasterxml.jackson.annotation.JsonProperty
import com.malinskiy.marathon.cli.config.ConfigurationException
import com.malinskiy.marathon.ios.IOSConfiguration
import java.io.File

interface FileListProvider {
    fun fileList(root: File = File(".")): Sequence<File>
}

object DerivedDataFileListProvider: FileListProvider {
    override fun fileList(root: File): Sequence<File> {
        return root.walkTopDown()
    }
}

data class FileIOSConfiguration(
        @JsonProperty("derivedDataDir") val derivedDataDir: File,
        @JsonProperty("xctestrunPath") val xctestrunPath: File?,
        @JsonProperty("remoteUsername") val remoteUsername: String,
        @JsonProperty("remotePublicKey") val remotePublicKey: File,
        @JsonProperty("sourceRoot") val sourceRoot: File?,
        val fileListProvider: FileListProvider = DerivedDataFileListProvider) : FileVendorConfiguration {

    fun toIOSConfiguration(xctestrunPathOverride: File? = null,
                           sourceRootOverride: File? = null): IOSConfiguration {

        val finalXCTestRunPath = xctestrunPathOverride
                ?: xctestrunPath
                ?: fileListProvider
                        .fileList(derivedDataDir)
                        .firstOrNull(extension = "xctestrun")
                ?: throw ConfigurationException("Unable to find xctestrun file in derived data folder")
        val optionalSourceRoot = sourceRootOverride ?: sourceRoot

        return if (optionalSourceRoot == null) {
            IOSConfiguration(derivedDataDir, finalXCTestRunPath, remoteUsername, remotePublicKey)
        } else {
            IOSConfiguration(derivedDataDir, finalXCTestRunPath, remoteUsername, remotePublicKey, optionalSourceRoot)
        }
    }
}

private fun Sequence<File>.firstOrNull(extension: String): File? {
    return firstOrNull { it.extension == extension }
}