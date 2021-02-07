package com.malinskiy.marathon.cli.schema

import com.malinskiy.marathon.cli.schema.android.AllureConfiguration
import com.malinskiy.marathon.cli.schema.android.DEFAULT_ALLURE_CONFIGURATION
import com.malinskiy.marathon.cli.schema.android.ScreenRecordConfiguration
import com.malinskiy.marathon.cli.schema.android.SerialStrategy
import com.malinskiy.marathon.cli.schema.android.VendorType
import java.io.File

sealed class VendorConfiguration {
    data class Android(
        val vendor: VendorType = VendorType.DDMLIB,
        val androidSdk: File,
        val applicationApk: File?,
        val testApplicationApk: File,
        val autoGrantPermission: Boolean?,
        val instrumentationArgs: Map<String, String>?,
        val applicationPmClear: Boolean?,
        val testApplicationPmClear: Boolean = false,
        val adbInitTimeoutMillis: Int?,
        val installOptions: String?,
        val serialStrategy: SerialStrategy = SerialStrategy.AUTOMATIC,
        val screenRecordConfiguration: ScreenRecordConfiguration = ScreenRecordConfiguration(),
        val waitForDevicesTimeoutMillis: Long?,
        val allureConfiguration: AllureConfiguration = DEFAULT_ALLURE_CONFIGURATION
    ) : VendorConfiguration()
    data class IOS(
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
}
