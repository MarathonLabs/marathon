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
import com.malinskiy.marathon.config.vendor.ios.AppleTestBundleConfiguration
import com.malinskiy.marathon.config.vendor.ios.LifecycleConfiguration
import com.malinskiy.marathon.config.vendor.ios.PermissionsConfiguration
import com.malinskiy.marathon.config.vendor.ios.RsyncConfiguration
import com.malinskiy.marathon.config.vendor.ios.SigningConfiguration
import com.malinskiy.marathon.config.vendor.ios.SshConfiguration
import com.malinskiy.marathon.config.vendor.ios.XcresultConfiguration
import java.io.File
import com.malinskiy.marathon.config.vendor.ios.ScreenRecordConfiguration as AppleScreenRecordConfiguration
import com.malinskiy.marathon.config.vendor.ios.ThreadingConfiguration as IosThreadingConfiguration
import com.malinskiy.marathon.config.vendor.ios.TimeoutConfiguration as AppleTimeoutConfiguration

const val DEFAULT_INIT_TIMEOUT_MILLIS = 30_000
const val DEFAULT_AUTO_GRANT_PERMISSION = false
const val DEFAULT_DISABLE_WINDOW_ANIMATION = true
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
    JsonSubTypes.Type(value = VendorConfiguration.EmptyVendorConfiguration::class, name = "empty"),
)
sealed class VendorConfiguration {
    data class AndroidConfiguration(
        @JsonProperty("androidSdk") val androidSdk: File?,
        @JsonProperty("applicationApk") val applicationOutput: File?,
        @JsonProperty("testApplicationApk") val testApplicationOutput: File?,
        @JsonProperty("splitApks") val splitApks: List<File>?,
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
        @JsonProperty("adbServers") val adbServers: List<AdbEndpoint> = listOf(AdbEndpoint()),
        @JsonProperty("disableWindowAnimation") val disableWindowAnimation: Boolean = DEFAULT_DISABLE_WINDOW_ANIMATION,
    ) : VendorConfiguration() {
        fun safeAndroidSdk(): File = androidSdk ?: throw ConfigurationException("No android SDK path specified")
    }

    class AndroidConfigurationBuilder {
        var androidSdk: File? = null
        var applicationOutput: File? = null
        var testApplicationOutput: File? = null
        var extraApplicationsOutput: List<File>? = null
        var splitApks: List<File>? = null
        var outputs: List<AndroidTestBundleConfiguration>? = null
        var autoGrantPermission: Boolean = DEFAULT_AUTO_GRANT_PERMISSION
        var instrumentationArgs: Map<String, String> = emptyMap()
        var applicationPmClear: Boolean = DEFAULT_APPLICATION_PM_CLEAR
        var testApplicationPmClear: Boolean = DEFAULT_TEST_APPLICATION_PM_CLEAR
        var adbInitTimeoutMillis: Int = DEFAULT_INIT_TIMEOUT_MILLIS
        var installOptions: String = DEFAULT_INSTALL_OPTIONS
        var serialStrategy: SerialStrategy = SerialStrategy.AUTOMATIC
        var screenRecordConfiguration: ScreenRecordConfiguration = ScreenRecordConfiguration()
        var waitForDevicesTimeoutMillis: Long = DEFAULT_WAIT_FOR_DEVICES_TIMEOUT
        var allureConfiguration: AllureConfiguration = AllureConfiguration()
        var timeoutConfiguration: TimeoutConfiguration = TimeoutConfiguration()
        var fileSyncConfiguration: FileSyncConfiguration = FileSyncConfiguration()
        var threadingConfiguration: ThreadingConfiguration = ThreadingConfiguration()
        var testParserConfiguration: TestParserConfiguration = TestParserConfiguration.LocalTestParserConfiguration
        var testAccessConfiguration: TestAccessConfiguration = TestAccessConfiguration()
        var adbServers: List<AdbEndpoint> = listOf(AdbEndpoint())
        var disableWindowAnimation: Boolean = DEFAULT_DISABLE_WINDOW_ANIMATION

        fun build() = AndroidConfiguration(
            androidSdk,
            applicationOutput,
            testApplicationOutput,
            splitApks,
            extraApplicationsOutput,
            outputs,
            autoGrantPermission,
            instrumentationArgs,
            applicationPmClear,
            testApplicationPmClear,
            adbInitTimeoutMillis,
            installOptions,
            serialStrategy,
            screenRecordConfiguration,
            waitForDevicesTimeoutMillis,
            allureConfiguration,
            timeoutConfiguration,
            fileSyncConfiguration,
            threadingConfiguration,
            testParserConfiguration,
            testAccessConfiguration,
            adbServers,
            disableWindowAnimation
        )
    }

    data class IOSConfiguration(
        @JsonProperty("bundle") val bundle: AppleTestBundleConfiguration? = null,
        @JsonProperty("devices") val devicesFile: File? = null,
        @JsonProperty("ssh") val ssh: SshConfiguration = SshConfiguration(),

        @JsonProperty("xcresult") val xcresult: XcresultConfiguration = XcresultConfiguration(),
        @JsonProperty("screenRecordConfiguration") val screenRecordConfiguration: AppleScreenRecordConfiguration = AppleScreenRecordConfiguration(),
        @JsonProperty("xctestrunEnv") val xctestrunEnv: Map<String, String> = emptyMap(),
        @JsonProperty("lifecycle") val lifecycleConfiguration: LifecycleConfiguration = LifecycleConfiguration(),
        @JsonProperty("permissions") val permissions: PermissionsConfiguration = PermissionsConfiguration(),
        @JsonProperty("timeoutConfiguration") val timeoutConfiguration: AppleTimeoutConfiguration = AppleTimeoutConfiguration(),
        @JsonProperty("threadingConfiguration") val threadingConfiguration: IosThreadingConfiguration = IosThreadingConfiguration(),
        @JsonProperty("hideRunnerOutput") val hideRunnerOutput: Boolean = false,
        @JsonProperty("compactOutput") val compactOutput: Boolean = false,
        @JsonProperty("rsync") val rsync: RsyncConfiguration = RsyncConfiguration(),
        @JsonProperty("xcodebuildTestArgs") val xcodebuildTestArgs: Map<String, String> = emptyMap(),
        @JsonProperty("testParserConfiguration") val testParserConfiguration: com.malinskiy.marathon.config.vendor.ios.TestParserConfiguration = com.malinskiy.marathon.config.vendor.ios.TestParserConfiguration.NmTestParserConfiguration(),

        @JsonProperty("signing") val signing: SigningConfiguration = SigningConfiguration(),
    ) : VendorConfiguration() {
        fun validate() {
            ssh.validate()

            val testBundleConfiguration = bundle ?: throw ConfigurationException("bundles must contain at least one valid entry")
            testBundleConfiguration.validate()
        }
    }

    @Suppress("CanSealedSubClassBeObject")
    class EmptyVendorConfiguration : VendorConfiguration()

    //For testing purposes
    object StubVendorConfiguration : VendorConfiguration()
}
