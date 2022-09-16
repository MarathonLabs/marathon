package com.malinskiy.marathon.config.vendor

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.vendor.android.AdbEndpoint
import com.malinskiy.marathon.config.vendor.android.AllureConfiguration
import com.malinskiy.marathon.config.vendor.android.AndroidTestBundleConfiguration
import com.malinskiy.marathon.config.vendor.android.FileSyncConfiguration
import com.malinskiy.marathon.config.vendor.android.ScreenRecordConfiguration
import com.malinskiy.marathon.config.vendor.android.SerialStrategy
import com.malinskiy.marathon.config.vendor.android.TestAccessConfiguration
import com.malinskiy.marathon.config.vendor.android.TestParserConfiguration
import com.malinskiy.marathon.config.vendor.android.ThreadingConfiguration
import com.malinskiy.marathon.config.vendor.android.TimeoutConfiguration
import java.io.File

const val DEFAULT_INIT_TIMEOUT_MILLIS = 30_000
const val DEFAULT_AUTO_GRANT_PERMISSION = false
const val DEFAULT_APPLICATION_PM_CLEAR = false
const val DEFAULT_TEST_APPLICATION_PM_CLEAR = false
const val DEFAULT_INSTALL_OPTIONS = ""
const val DEFAULT_WAIT_FOR_DEVICES_TIMEOUT = 30000L

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = VendorConfiguration.AndroidConfiguration::class, name = "Android"),
    JsonSubTypes.Type(value = VendorConfiguration.IOSConfiguration::class, name = "iOS"),
    JsonSubTypes.Type(value = VendorConfiguration.StubVendorConfiguration::class, name = "stub"),
)
sealed class VendorConfiguration {
    data class AndroidConfiguration(
        @JsonProperty("vendor") val vendor: VendorType = VendorType.ADAM,
        @JsonProperty("androidSdk") val androidSdk: File?,
        @JsonProperty("applicationApk") val applicationOutput: File?,
        @JsonProperty("testApplicationApk") val testApplicationOutput: File?,
        @JsonProperty("extraApplicationsApk") val extraApplicationsOutput: List<File>?,
        @JsonProperty("outputs") val outputs: List<AndroidTestBundleConfiguration>? = null,
        @JsonProperty("autoGrantPermission") val autoGrantPermission: Boolean = DEFAULT_AUTO_GRANT_PERMISSION,
        @JsonProperty("instrumentationArgs") val instrumentationArgs: Map<String, String> = emptyMap(),
        @JsonProperty("applicationPmClear") val applicationPmClear: Boolean = DEFAULT_APPLICATION_PM_CLEAR,
        @JsonProperty("testApplicationPmClear") val testApplicationPmClear: Boolean = DEFAULT_TEST_APPLICATION_PM_CLEAR,
        @JsonProperty("adbInitTimeoutMillis") val adbInitTimeoutMillis: Int = DEFAULT_INIT_TIMEOUT_MILLIS,
        @JsonProperty("installOptions") val installOptions: String = DEFAULT_INSTALL_OPTIONS,
        @JsonProperty("serialStrategy") val serialStrategy: SerialStrategy = SerialStrategy.AUTOMATIC,
        @JsonProperty("screenRecordConfiguration") val screenRecordConfiguration: ScreenRecordConfiguration = ScreenRecordConfiguration(),
        @JsonProperty("waitForDevicesTimeoutMillis") val waitForDevicesTimeoutMillis: Long = DEFAULT_WAIT_FOR_DEVICES_TIMEOUT,
        @JsonProperty("allureConfiguration") val allureConfiguration: AllureConfiguration = AllureConfiguration(),
        @JsonProperty("timeoutConfiguration") val timeoutConfiguration: TimeoutConfiguration = TimeoutConfiguration(),
        @JsonProperty("fileSyncConfiguration") val fileSyncConfiguration: FileSyncConfiguration = FileSyncConfiguration(),
        @JsonProperty("threadingConfiguration") val threadingConfiguration: ThreadingConfiguration = ThreadingConfiguration(),
        @JsonProperty("testParserConfiguration") val testParserConfiguration: TestParserConfiguration = TestParserConfiguration.LocalTestParserConfiguration,
        @JsonProperty("testAccessConfiguration") val testAccessConfiguration: TestAccessConfiguration = TestAccessConfiguration(),
        @JsonProperty("adbServers") val adbServers: List<AdbEndpoint> = listOf(AdbEndpoint())
    ) : VendorConfiguration() {
        fun safeAndroidSdk(): File = androidSdk ?: throw ConfigurationException("No android SDK path specified")

        enum class VendorType {
            DDMLIB,
            ADAM
        }
    }

    data class IOSConfiguration(
        @JsonProperty("derivedDataDir") val derivedDataDir: File,
        @JsonProperty("xctestrunPath") val xctestrunPath: File?,
        @JsonProperty("remoteUsername") val remoteUsername: String,
        @JsonProperty("remotePrivateKey") val remotePrivateKey: File,
        @JsonProperty("knownHostsPath") val knownHostsPath: File?,
        @JsonProperty("remoteRsyncPath") val remoteRsyncPath: String = "/usr/bin/rsync",
        @JsonProperty("sourceRoot") val sourceRoot: File = File("."),
        @JsonProperty("alwaysEraseSimulators") val alwaysEraseSimulators: Boolean = true,
        @JsonProperty("debugSsh") val debugSsh: Boolean = false,
        @JsonProperty("hideRunnerOutput") val hideRunnerOutput: Boolean = false,
        @JsonProperty("compactOutput") val compactOutput: Boolean = false,
        @JsonProperty("keepAliveIntervalMillis") val keepAliveIntervalMillis: Long = 0L,
        @JsonProperty("xcResultBundlePath") val xcResultBundlePath: File,
        @JsonProperty("devices") val devicesFile: File? = null,
    ) : VendorConfiguration() {
        /**
         * Exception should not happen since it will be first thrown in deserializer
         */
        fun safecxtestrunPath(): File =
            xctestrunPath ?: throw ConfigurationException("Unable to find an xctestrun file in derived data folder")
    }

    //For testing purposes
    object StubVendorConfiguration : VendorConfiguration()
}
