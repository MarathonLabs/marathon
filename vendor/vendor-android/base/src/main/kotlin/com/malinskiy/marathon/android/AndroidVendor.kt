package com.malinskiy.marathon.android

import com.malinskiy.marathon.android.configuration.AndroidLogConfigurator
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.execution.bundle.TestBundleIdentifier
import com.malinskiy.marathon.log.MarathonLogConfigurator
import org.koin.dsl.module

val AndroidVendor = module {
    val testBundleIdentifier = AndroidTestBundleIdentifier()
    single<TestParser?> { AndroidTestParser(get(), get()) }
    single<TestBundleIdentifier?> { testBundleIdentifier }
    single { testBundleIdentifier }
    single<MarathonLogConfigurator> { AndroidLogConfigurator() }
}
