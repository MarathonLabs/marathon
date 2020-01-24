package com.malinskiy.marathon.cli.args

import com.fasterxml.jackson.annotation.JsonProperty
import com.malinskiy.marathon.android.configuration.AllureConfiguration
import com.malinskiy.marathon.android.configuration.AndroidConfiguration
import com.malinskiy.marathon.android.configuration.DEFAULT_ALLURE_CONFIGURATION
import com.malinskiy.marathon.android.configuration.DEFAULT_INSTALL_OPTIONS
import com.malinskiy.marathon.android.configuration.SerialStrategy
import com.malinskiy.marathon.android.configuration.defaultInitTimeoutMillis
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.exceptions.ConfigurationException
import ddmlibModule
import java.io.File

data class FileAndroidConfiguration(
    @JsonProperty("vendor") val vendor: String? = "ddmlib",
    @JsonProperty("androidSdk") val androidSdk: File?,
    @JsonProperty("applicationApk") val applicationOutput: File?,
    @JsonProperty("testApplicationApk") val testApplicationOutput: File,
    @JsonProperty("autoGrantPermission") val autoGrantPermission: Boolean?,
    @JsonProperty("instrumentationArgs") val instrumentationArgs: Map<String, String>?,
    @JsonProperty("applicationPmClear") val applicationPmClear: Boolean?,
    @JsonProperty("testApplicationPmClear") val testApplicationPmClear: Boolean?,
    @JsonProperty("adbInitTimeoutMillis") val adbInitTimeoutMillis: Int?,
    @JsonProperty("installOptions") val installOptions: String?,
    @JsonProperty("preferableRecorderType") val preferableRecorderType: DeviceFeature?,
    @JsonProperty("serialStrategy") val serialStrategy: SerialStrategy = SerialStrategy.AUTOMATIC,
    @JsonProperty("allureConfiguration") val allureConfiguration: AllureConfiguration?
) : FileVendorConfiguration {

    fun toAndroidConfiguration(environmentAndroidSdk: File?): AndroidConfiguration {
        val finalAndroidSdk = androidSdk
            ?: environmentAndroidSdk
            ?: throw ConfigurationException("No android SDK path specified")

        when (vendor) {
            else -> {
                return AndroidConfiguration(
                    androidSdk = finalAndroidSdk,
                    applicationOutput = applicationOutput,
                    testApplicationOutput = testApplicationOutput,
                    autoGrantPermission = autoGrantPermission ?: false,
                    instrumentationArgs = instrumentationArgs ?: emptyMap(),
                    applicationPmClear = applicationPmClear ?: false,
                    testApplicationPmClear = testApplicationPmClear ?: false,
                    adbInitTimeoutMillis = adbInitTimeoutMillis
                        ?: defaultInitTimeoutMillis,
                    installOptions = installOptions
                        ?: DEFAULT_INSTALL_OPTIONS,
                    preferableRecorderType = preferableRecorderType,
                    serialStrategy = serialStrategy,
                    implementationModules = listOf(ddmlibModule),
                    allureConfiguration = allureConfiguration
                        ?: DEFAULT_ALLURE_CONFIGURATION
                )
            }
        }
    }
}
