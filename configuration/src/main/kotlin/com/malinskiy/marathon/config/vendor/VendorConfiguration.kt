package com.malinskiy.marathon.config.vendor

import com.malinskiy.marathon.config.vendor.android.AllureConfiguration
import com.malinskiy.marathon.config.vendor.android.AndroidTestBundleConfiguration
import com.malinskiy.marathon.config.vendor.android.FileSyncConfiguration
import com.malinskiy.marathon.config.vendor.android.ScreenRecordConfiguration
import com.malinskiy.marathon.config.vendor.android.SerialStrategy
import com.malinskiy.marathon.config.vendor.android.ThreadingConfiguration
import com.malinskiy.marathon.config.vendor.android.TimeoutConfiguration
import java.io.File

const val DEFAULT_INIT_TIMEOUT_MILLIS = 30_000
const val DEFAULT_AUTO_GRANT_PERMISSION = false
const val DEFAULT_APPLICATION_PM_CLEAR = false
const val DEFAULT_TEST_APPLICATION_PM_CLEAR = false
const val DEFAULT_INSTALL_OPTIONS = ""
const val DEFAULT_WAIT_FOR_DEVICES_TIMEOUT = 30000L

sealed class VendorConfiguration {
    data class AndroidConfiguration(
        val androidSdk: File,
        val applicationOutput: File?,
        val testApplicationOutput: File?,
        val outputs: List<AndroidTestBundleConfiguration>? = null,
        val autoGrantPermission: Boolean = DEFAULT_AUTO_GRANT_PERMISSION,
        val instrumentationArgs: Map<String, String> = emptyMap(),
        val applicationPmClear: Boolean = DEFAULT_APPLICATION_PM_CLEAR,
        val testApplicationPmClear: Boolean = DEFAULT_TEST_APPLICATION_PM_CLEAR,
        val adbInitTimeoutMillis: Int = DEFAULT_INIT_TIMEOUT_MILLIS,
        val installOptions: String = DEFAULT_INSTALL_OPTIONS,
        val serialStrategy: SerialStrategy = SerialStrategy.AUTOMATIC,
        val screenRecordConfiguration: ScreenRecordConfiguration = ScreenRecordConfiguration(),
        val waitForDevicesTimeoutMillis: Long = DEFAULT_WAIT_FOR_DEVICES_TIMEOUT,
        val allureConfiguration: AllureConfiguration = AllureConfiguration(),
        val timeoutConfiguration: TimeoutConfiguration = TimeoutConfiguration(),
        val fileSyncConfiguration: FileSyncConfiguration = FileSyncConfiguration(),
        val threadingConfiguration: ThreadingConfiguration = ThreadingConfiguration(),
    ) : VendorConfiguration()

    data class IOSConfiguration(
        val derivedDataDir: File,
        val xctestrunPath: File,
        val remoteUsername: String,
        val remotePrivateKey: File,
        val knownHostsPath: File?,
        val remoteRsyncPath: String,
        val debugSsh: Boolean,
        val alwaysEraseSimulators: Boolean,
        val hideRunnerOutput: Boolean = false,
        val compactOutput: Boolean = false,
        val keepAliveIntervalMillis: Long = 0L,
        val devicesFile: File? = null,
        val sourceRoot: File = File(".")
    ) : VendorConfiguration()
    
    //For testing purposes
    object StubVendorConfiguration : VendorConfiguration()
}
