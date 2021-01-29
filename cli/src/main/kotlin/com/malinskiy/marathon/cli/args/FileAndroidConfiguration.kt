package com.malinskiy.marathon.cli.args

import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.android.DEFAULT_INSTALL_OPTIONS
import com.malinskiy.marathon.android.DEFAULT_WAIT_FOR_DEVICES_TIMEOUT
import com.malinskiy.marathon.android.ScreenRecordConfiguration
import com.malinskiy.marathon.android.VendorType
import com.malinskiy.marathon.android.adam.di.adamModule
import com.malinskiy.marathon.android.configuration.AllureConfiguration
import com.malinskiy.marathon.android.configuration.DEFAULT_ALLURE_CONFIGURATION
import com.malinskiy.marathon.android.configuration.SerialStrategy
import com.malinskiy.marathon.android.defaultInitTimeoutMillis
import ddmlibModule
import java.io.File

data class FileAndroidConfiguration(
    val vendor: VendorType = VendorType.DDMLIB,
    val androidSdk: File,
    val applicationApk: File?,
    val testApplicationApk: File,
    val autoGrantPermission: Boolean?,
    val instrumentationArgs: Map<String, String>?,
    val applicationPmClear: Boolean?,
    val testApplicationPmClear: Boolean?,
    val adbInitTimeoutMillis: Int?,
    val installOptions: String?,
    val serialStrategy: SerialStrategy = SerialStrategy.AUTOMATIC,
    val screenRecordConfiguration: ScreenRecordConfiguration = ScreenRecordConfiguration(),
    val waitForDevicesTimeoutMillis: Long?,
    val allureConfiguration: AllureConfiguration?
) : FileVendorConfiguration {

    fun toAndroidConfiguration(): AndroidConfiguration {
        val implementationModules = when (vendor) {
            VendorType.ADAM -> listOf(adamModule)
            VendorType.DDMLIB -> listOf(ddmlibModule)
        }

        return AndroidConfiguration(
            androidSdk = androidSdk,
            applicationApk = applicationApk,
            testApplicationApk = testApplicationApk,
            autoGrantPermission = autoGrantPermission ?: false,
            instrumentationArgs = instrumentationArgs ?: emptyMap(),
            applicationPmClear = applicationPmClear ?: false,
            testApplicationPmClear = testApplicationPmClear ?: false,
            adbInitTimeoutMillis = adbInitTimeoutMillis ?: defaultInitTimeoutMillis,
            installOptions = installOptions ?: DEFAULT_INSTALL_OPTIONS,
            serialStrategy = serialStrategy,
            screenRecordConfiguration = screenRecordConfiguration,
            waitForDevicesTimeoutMillis = waitForDevicesTimeoutMillis ?: DEFAULT_WAIT_FOR_DEVICES_TIMEOUT,
            implementationModules = implementationModules,
            allureConfiguration = allureConfiguration
                ?: DEFAULT_ALLURE_CONFIGURATION
        )
    }
}
