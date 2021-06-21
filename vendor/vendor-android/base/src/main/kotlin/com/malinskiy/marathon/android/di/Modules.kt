package com.malinskiy.marathon.android.di

import com.malinskiy.marathon.android.AndroidTestBundleIdentifier
import com.malinskiy.marathon.android.AndroidTestParser
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.execution.bundle.TestBundleIdentifier
import org.koin.dsl.module

val androidModule = module {
    val testBundleIdentifier = AndroidTestBundleIdentifier()
    single<TestParser?> { AndroidTestParser(get()) }
    single<TestBundleIdentifier?> { testBundleIdentifier }
    single { testBundleIdentifier }
}
