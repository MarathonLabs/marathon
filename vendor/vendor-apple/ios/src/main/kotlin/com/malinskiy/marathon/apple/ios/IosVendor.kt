package com.malinskiy.marathon.apple.ios

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.gson.GsonBuilder
import com.malinskiy.marathon.apple.AppleApplicationInstaller
import com.malinskiy.marathon.apple.AppleLogConfigurator
import com.malinskiy.marathon.apple.AppleTestBundleIdentifier
import com.malinskiy.marathon.apple.NmTestParser
import com.malinskiy.marathon.apple.XCTestParser
import com.malinskiy.marathon.apple.bin.xcrun.simctl.model.SimctlDeviceList
import com.malinskiy.marathon.apple.bin.xcrun.simctl.model.SimctlDeviceListDeserializer
import com.malinskiy.marathon.apple.ios.device.AppleSimulatorProvider
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.apple.TestParserConfiguration
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.execution.bundle.TestBundleIdentifier
import com.malinskiy.marathon.log.MarathonLogConfigurator
import org.koin.dsl.module

val IosVendor = module {
    single<DeviceProvider> {
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
        AppleSimulatorProvider(get(), get(), get(), gson, objectMapper, get(), get())
    }
    single<TestParser?> {
        val configuration = get<Configuration>()
        val iosConfiguration = configuration.vendorConfiguration as? VendorConfiguration.IOSConfiguration
        val testParserConfiguration = iosConfiguration?.testParserConfiguration
        when {
            testParserConfiguration != null && testParserConfiguration is TestParserConfiguration.XCTestParserConfiguration -> XCTestParser<AppleSimulatorDevice>(
                get(),
                get(),
                get(),
                get(),
            )

            testParserConfiguration != null && testParserConfiguration is TestParserConfiguration.NmTestParserConfiguration -> NmTestParser(
                get(),
                get(),
                testParserConfiguration,
                get()
            )

            else -> NmTestParser(get(), get(), TestParserConfiguration.NmTestParserConfiguration(), get())
        }
    }
    single<AppleApplicationInstaller<AppleSimulatorDevice>> { AppleSimulatorApplicationInstaller(get()) }
    single<MarathonLogConfigurator> { AppleLogConfigurator(get<VendorConfiguration.IOSConfiguration>().compactOutput) }

    val appleTestBundleIdentifier = AppleTestBundleIdentifier()
    single<TestBundleIdentifier?> { appleTestBundleIdentifier }
    single<AppleTestBundleIdentifier> { appleTestBundleIdentifier }
}
