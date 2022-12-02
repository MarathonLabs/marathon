package com.malinskiy.marathon.android.adam

import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.android.AllureConfiguration
import com.malinskiy.marathon.config.vendor.android.FileSyncConfiguration
import java.io.File

object TestConfigurationFactory {
    fun create(
        autoGrantPermission: Boolean = false,
        installOptions: String = "",
        fileSyncConfiguration: FileSyncConfiguration = FileSyncConfiguration(),
        allureConfiguration: AllureConfiguration = AllureConfiguration(),
        isCodeCoverageEnabled: Boolean = false,
        applicationOutput: File? = File(javaClass.classLoader.getResource("apk/app-debug.apk").file),
        testApplicationOutput: File? = File(javaClass.classLoader.getResource("apk/app-debug-androidTest.apk").file),
        splitApks: List<File>? = emptyList(),
        extraApplicationsOutput: List<File>? = emptyList(),
    ): Configuration {
        return Configuration.Builder(
            name = "",
            outputDir = File(""),
        ).apply {
            vendorConfiguration = VendorConfiguration.AndroidConfiguration(
                androidSdk = File(""),
                applicationOutput = applicationOutput,
                testApplicationOutput = testApplicationOutput,
                splitApks = splitApks,
                extraApplicationsOutput = extraApplicationsOutput,
                autoGrantPermission = autoGrantPermission,
                installOptions = installOptions,
                fileSyncConfiguration = fileSyncConfiguration,
                allureConfiguration = allureConfiguration
            )
            this.isCodeCoverageEnabled = isCodeCoverageEnabled
            strictMode = false
            debug = false
            analyticsTracking = false
        }.build()
    }
}
