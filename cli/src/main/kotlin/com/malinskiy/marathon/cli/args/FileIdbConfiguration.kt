package com.malinskiy.marathon.cli.args

import com.fasterxml.jackson.annotation.JsonProperty
import com.malinskiy.marathon.ios.idb.configuration.IdbConfiguration
import com.malinskiy.marathon.vendor.VendorConfiguration
import java.io.File


private fun File.resolveAgainst(file: File): File = file.resolve(this)

data class FileIdbConfiguration(
    @JsonProperty("derivedDataDir") val derivedDataDir: File,
    @JsonProperty("xctestrunPath") val xctestrunPath: File?,
    @JsonProperty("app") val app: File,
    @JsonProperty("runner") val runner: File,
    @JsonProperty("idbHosts") val idbHosts: File
) : FileVendorConfiguration {
    fun toIdbConfiguration(marathonfileDir: File): VendorConfiguration {
        val fileListProvider: FileListProvider = DerivedDataFileListProvider
        val resolvedDerivedDataDir = marathonfileDir.resolve(derivedDataDir)
        val finalXCTestRunPath = xctestrunPath?.resolveAgainst(marathonfileDir)
            ?: fileListProvider
                .fileList(resolvedDerivedDataDir)
                .firstOrNull { it.extension == "xctestrun" }

        return IdbConfiguration(idbHosts, app, runner, finalXCTestRunPath!!);
    }
}
