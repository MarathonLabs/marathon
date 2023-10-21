package com.malinskiy.marathon.ios

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.gson.GsonBuilder
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.ios.TestParserConfiguration
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.execution.bundle.TestBundleIdentifier
import com.malinskiy.marathon.ios.bin.xcrun.simctl.model.SimctlDeviceList
import com.malinskiy.marathon.ios.bin.xcrun.simctl.model.SimctlDeviceListDeserializer
import com.malinskiy.marathon.log.MarathonLogConfigurator
import org.koin.dsl.module

val AppleVendor = module {
    single <DeviceProvider> {
            val gson = GsonBuilder()
                .registerTypeAdapter(SimctlDeviceList::class.java, SimctlDeviceListDeserializer())
                .create()
            val objectMapper = ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID))
                .registerModule(
                    KotlinModule.Builder()
                        .withReflectionCacheSize(512)
                        .configure(KotlinFeature.NullToEmptyCollection, false)
                        .configure(KotlinFeature.NullToEmptyMap, false)
                        .configure(KotlinFeature.NullIsSameAsDefault, false)
                        .configure(KotlinFeature.SingletonSupport, true)
                        .configure(KotlinFeature.StrictNullChecks, false)
                        .build()
                )
            AppleDeviceProvider(get(), get(), get(), gson, objectMapper, get(), get())
    }
    single<TestParser?> {
        val configuration = get<Configuration>()
        val iosConfiguration = configuration.vendorConfiguration as? VendorConfiguration.IOSConfiguration
        val testParserConfiguration = iosConfiguration?.testParserConfiguration
        when {
            testParserConfiguration != null && testParserConfiguration is TestParserConfiguration.XCTestParserConfiguration -> XCTestParser(
                get(),
                get(),
                get()
            )

            else -> NmTestParser(get(), get(), get())
        }
    }
    single<MarathonLogConfigurator> { AppleLogConfigurator(get()) }

    val appleTestBundleIdentifier = AppleTestBundleIdentifier()
    single<TestBundleIdentifier?> { appleTestBundleIdentifier }
    single<AppleTestBundleIdentifier> { appleTestBundleIdentifier }
}
