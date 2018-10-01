package com.malinskiy.marathon.cli.args

import com.fasterxml.jackson.annotation.JsonProperty
import com.malinskiy.marathon.cli.config.ConfigurationException
import com.malinskiy.marathon.ios.IOSConfiguration
import java.io.File

interface FileListProvider {
    fun fileList(root: File = File(".")): Iterable<File>
}

object DerivedDataFileListProvider: FileListProvider {
    override fun fileList(root: File): Iterable<File> {
        return root.walkTopDown().asIterable()
    }
}

data class FileIOSConfiguration(
        @JsonProperty("derivedDataDir") val derivedDataDir: File,
        @JsonProperty("xctestrunPath") val xctestrunPath: File?,
        @JsonProperty("remoteUsername") val remoteUsername: String,
        @JsonProperty("remotePrivateKey") val remotePrivateKey: File,
        @JsonProperty("sourceRoot") val sourceRoot: File?,
        val fileListProvider: FileListProvider = DerivedDataFileListProvider) : FileVendorConfiguration {

    fun toIOSConfiguration(marathonfileDir: File,
                           xctestrunPathOverride: File? = null,
                           sourceRootOverride: File? = null): IOSConfiguration {
        // Any relative path specified in Marathonfile should be resolved against the directory it's in
        val resolvedDerivedDataDir = marathonfileDir.resolve(derivedDataDir)
        val finalXCTestRunPath = xctestrunPathOverride
                ?: xctestrunPath?.resolveAgainst(marathonfileDir)
                ?: fileListProvider
                        .fileList(resolvedDerivedDataDir)
                        .firstOrNull { it.extension == "xctestrun" }
                ?: throw ConfigurationException("Unable to find an xctestrun file in derived data folder")
        val optionalSourceRoot = sourceRootOverride
                ?: sourceRoot?.resolveAgainst(marathonfileDir)

        return if (optionalSourceRoot == null) {
            IOSConfiguration(resolvedDerivedDataDir, finalXCTestRunPath, remoteUsername, remotePrivateKey)
        } else {
            IOSConfiguration(resolvedDerivedDataDir, finalXCTestRunPath, remoteUsername, remotePrivateKey, optionalSourceRoot)
        }
    }
}

// inverted [resolve] call allows to avoid too many if expressions
private fun File.resolveAgainst(file: File): File = file.resolve(this)