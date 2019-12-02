package com.malinskiy.marathon.android.di

import com.malinskiy.marathon.android.AndroidComponentInfoExtractor
import com.malinskiy.marathon.android.AndroidDeviceProvider
import com.malinskiy.marathon.android.AndroidTestParser
import com.malinskiy.marathon.android.executor.AndroidAppInstaller
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.ComponentInfoExtractor
import com.malinskiy.marathon.execution.TestParser
import org.koin.dsl.module

val androidModule = module {
    single<DeviceProvider?> { AndroidDeviceProvider(get(), get(), get(), get()) }
    single<AndroidAppInstaller?> { AndroidAppInstaller(get(), get()) }
    single<TestParser?> { AndroidTestParser() }
    single<ComponentInfoExtractor?> { AndroidComponentInfoExtractor() }
}