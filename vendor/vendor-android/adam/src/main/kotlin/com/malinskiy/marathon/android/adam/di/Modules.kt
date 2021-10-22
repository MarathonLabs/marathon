package com.malinskiy.marathon.android.adam.di

import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.android.AndroidTestParser
import com.malinskiy.marathon.android.adam.AdamDeviceProvider
import com.malinskiy.marathon.android.adam.OnDeviceTestParser
import com.malinskiy.marathon.android.configuration.TestParserConfiguration
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestParser
import org.koin.dsl.module

val adamModule = module {
    single<DeviceProvider> { AdamDeviceProvider(get(), get(), get(), get(), get()) }
    single<TestParser?>(override = true) {
        val configuration = get<Configuration>()
        val androidConfiguration = configuration.vendorConfiguration as? AndroidConfiguration
        val testParserConfiguration = androidConfiguration?.testParserConfiguration
        when {
            testParserConfiguration != null && testParserConfiguration is TestParserConfiguration.RemoteTestParser -> OnDeviceTestParser(get())
            else -> AndroidTestParser(get())
        }
    }
}
