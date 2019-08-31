package com.malinskiy.marathon.android.di

import com.malinskiy.marathon.android.AndroidDeviceProvider
import com.malinskiy.marathon.android.AndroidTestParser
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestParser
import org.koin.dsl.module

val androidModule = module {
    single<DeviceProvider?> { AndroidDeviceProvider(get()) }
    single<TestParser?> { AndroidTestParser() }
}