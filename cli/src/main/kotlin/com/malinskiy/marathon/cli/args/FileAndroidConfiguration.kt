package com.malinskiy.marathon.cli.args

import com.fasterxml.jackson.annotation.JsonProperty
import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.android.defaultInitTimeoutMillis
import com.malinskiy.marathon.exceptions.ConfigurationException
import java.io.File

data class FileAndroidConfiguration(
        @JsonProperty("androidSdk") val androidSdk: File?,
        @JsonProperty("applicationApk") val applicationOutput: File?,
        @JsonProperty("testApplicationApk") val testApplicationOutput: File,
        @JsonProperty("autoGrantPermission") val autoGrantPermission: Boolean?,
        @JsonProperty("instrumentationArgs") val instrumentationArgs: Map<String, String>?,
        @JsonProperty("adbInitTimeoutMillis") val adbInitTimeoutMillis: Int?)
    : FileVendorConfiguration {

    fun toAndroidConfiguration(environmentAndroidSdk: File?): AndroidConfiguration {
        val finalAndroidSdk = androidSdk
                ?: environmentAndroidSdk
                ?: throw ConfigurationException("No android SDK path specified")

        return AndroidConfiguration(
                androidSdk = finalAndroidSdk,
                applicationOutput = applicationOutput,
                testApplicationOutput = testApplicationOutput,
                autoGrantPermission = autoGrantPermission ?: false,
                instrumentationArgs = instrumentationArgs ?: emptyMap(),
                adbInitTimeoutMillis = adbInitTimeoutMillis ?: defaultInitTimeoutMillis
        )
    }
}
