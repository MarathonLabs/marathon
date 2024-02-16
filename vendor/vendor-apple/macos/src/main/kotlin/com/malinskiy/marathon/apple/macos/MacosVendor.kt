package com.malinskiy.marathon.apple.macos

import com.malinskiy.marathon.apple.AppleLogConfigurator
import com.malinskiy.marathon.apple.AppleTestBundleIdentifier
import com.malinskiy.marathon.apple.NmTestParser
import com.malinskiy.marathon.apple.XCTestParser
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.apple.TestParserConfiguration
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.execution.bundle.TestBundleIdentifier
import com.malinskiy.marathon.log.MarathonLogConfigurator
import org.koin.dsl.module

val MacosVendor = module {
    single<TestParser?> {
        val configuration = get<Configuration>()
        val iosConfiguration = configuration.vendorConfiguration as? VendorConfiguration.IOSConfiguration
        val testParserConfiguration = iosConfiguration?.testParserConfiguration
        when {
            testParserConfiguration != null && testParserConfiguration is TestParserConfiguration.XCTestParserConfiguration -> XCTestParser<MacosDevice>(
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
    single<MarathonLogConfigurator> { AppleLogConfigurator(get()) }

    val appleTestBundleIdentifier = AppleTestBundleIdentifier()
    single<TestBundleIdentifier?> { appleTestBundleIdentifier }
    single<AppleTestBundleIdentifier> { appleTestBundleIdentifier }
}
