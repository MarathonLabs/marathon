package com.malinskiy.marathon.cli.args

import com.fasterxml.jackson.annotation.JsonProperty
import com.malinskiy.marathon.ios.IOSConfiguration
import java.io.File
import javax.naming.ConfigurationException

data class FileIOSConfiguration(
        @JsonProperty("xctestrunPath") val xctestrunPath: File?,
        @JsonProperty("remoteUsername") val remoteUsername: String,
        @JsonProperty("remotePublicKey") val remotePublicKey: File,
        @JsonProperty("sourceRoot") val sourceRoot: File?) : FileVendorConfiguration {

    fun toIOSConfiguration(xctestrunPathOverride: File? = null,
                           sourceRootOverride: File? = null): IOSConfiguration {

        val finalXCTestRunPath = xctestrunPathOverride
                ?: xctestrunPath
                ?: throw ConfigurationException("No xctestrunPath specified")

        val optionalSourceRoot = sourceRootOverride ?: sourceRoot

        return if (optionalSourceRoot == null) {
            IOSConfiguration(finalXCTestRunPath, remoteUsername, remotePublicKey)
        } else {
            IOSConfiguration(finalXCTestRunPath, remoteUsername, remotePublicKey, optionalSourceRoot)
        }
    }
}