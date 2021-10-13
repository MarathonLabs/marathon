package com.malinskiy.marathon.cli.args

import com.fasterxml.jackson.annotation.JsonProperty
import com.malinskiy.marathon.android.VendorType
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.vendor.DEFAULT_INIT_TIMEOUT_MILLIS
import com.malinskiy.marathon.config.vendor.DEFAULT_INSTALL_OPTIONS
import com.malinskiy.marathon.config.vendor.DEFAULT_WAIT_FOR_DEVICES_TIMEOUT
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.android.AllureConfiguration
import com.malinskiy.marathon.config.vendor.android.AndroidTestBundleConfiguration
import com.malinskiy.marathon.config.vendor.android.FileSyncConfiguration
import com.malinskiy.marathon.config.vendor.android.ScreenRecordConfiguration
import com.malinskiy.marathon.config.vendor.android.SerialStrategy
import com.malinskiy.marathon.config.vendor.android.ThreadingConfiguration
import com.malinskiy.marathon.config.vendor.android.TimeoutConfiguration
import java.io.File

data class FileAndroidConfiguration(
    @JsonProperty("vendor") val vendor: VendorType = VendorType.DDMLIB,
    @JsonProperty("androidSdk") val androidSdk: File?,
    @JsonProperty("applicationApk") val applicationOutput: File?,
    @JsonProperty("testApplicationApk") val testApplicationOutput: File?,
    @JsonProperty("outputs") val outputs: List<AndroidTestBundleConfiguration>?,
    @JsonProperty("autoGrantPermission") val autoGrantPermission: Boolean?,
    @JsonProperty("instrumentationArgs") val instrumentationArgs: Map<String, String>?,
    @JsonProperty("applicationPmClear") val applicationPmClear: Boolean?,
    @JsonProperty("testApplicationPmClear") val testApplicationPmClear: Boolean?,
    @JsonProperty("adbInitTimeoutMillis") val adbInitTimeoutMillis: Int?,
    @JsonProperty("installOptions") val installOptions: String?,
    @JsonProperty("serialStrategy") val serialStrategy: SerialStrategy = SerialStrategy.AUTOMATIC,
    @JsonProperty("screenRecordConfiguration") val screenRecordConfiguration: ScreenRecordConfiguration = ScreenRecordConfiguration(),
    @JsonProperty("waitForDevicesTimeoutMillis") val waitForDevicesTimeoutMillis: Long?,
    @JsonProperty("allureConfiguration") val allureConfiguration: AllureConfiguration?,
    @JsonProperty("timeoutConfiguration") val timeoutConfiguration: TimeoutConfiguration = TimeoutConfiguration(),
    @JsonProperty("fileSyncConfiguration") val fileSyncConfiguration: FileSyncConfiguration = FileSyncConfiguration(),
    @JsonProperty("threadingConfiguration") val threadingConfiguration: ThreadingConfiguration = ThreadingConfiguration(),
) : FileVendorConfiguration {

    fun toAndroidConfiguration(environmentAndroidSdk: File?): VendorConfiguration.AndroidConfiguration {
        val finalAndroidSdk = androidSdk
            ?: environmentAndroidSdk
            ?: throw ConfigurationException("No android SDK path specified")

        return VendorConfiguration.AndroidConfiguration(
            androidSdk = finalAndroidSdk,
            applicationOutput = applicationOutput,
            testApplicationOutput = testApplicationOutput,
            outputs = outputs,
            autoGrantPermission = autoGrantPermission ?: false,
            instrumentationArgs = instrumentationArgs ?: emptyMap(),
            applicationPmClear = applicationPmClear ?: false,
            testApplicationPmClear = testApplicationPmClear ?: false,
            adbInitTimeoutMillis = adbInitTimeoutMillis ?: DEFAULT_INIT_TIMEOUT_MILLIS,
            installOptions = installOptions ?: DEFAULT_INSTALL_OPTIONS,
            serialStrategy = serialStrategy,
            screenRecordConfiguration = screenRecordConfiguration,
            waitForDevicesTimeoutMillis = waitForDevicesTimeoutMillis ?: DEFAULT_WAIT_FOR_DEVICES_TIMEOUT,
            allureConfiguration = allureConfiguration ?: AllureConfiguration(),
            timeoutConfiguration = timeoutConfiguration,
            fileSyncConfiguration = fileSyncConfiguration,
            threadingConfiguration = threadingConfiguration,
        )
    }
}
