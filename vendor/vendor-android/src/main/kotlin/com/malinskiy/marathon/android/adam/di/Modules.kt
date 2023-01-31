package com.malinskiy.marathon.android.adam.di

import com.malinskiy.marathon.android.DexTestParser
import com.malinskiy.marathon.android.adam.AdamDeviceProvider
import com.malinskiy.marathon.android.adam.AmInstrumentTestParser
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.android.TestParserConfiguration
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestParser
import org.koin.dsl.module

val adamModule = module {
    single<DeviceProvider> {
        AdamDeviceProvider(get(), get(), get(), get(), get())
    }
    single<TestParser?> {
        val configuration = get<Configuration>()
        val androidConfiguration = configuration.vendorConfiguration as? VendorConfiguration.AndroidConfiguration
        val testParserConfiguration = androidConfiguration?.testParserConfiguration
        when {
            testParserConfiguration != null && testParserConfiguration is TestParserConfiguration.RemoteTestParserConfiguration -> AmInstrumentTestParser(
                get(),
                get(),
                get()
            )

            else -> DexTestParser(get(), get())
        }
    }
}
