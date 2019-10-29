package com.malinskiy.marathon.ios.di

import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.ios.IOSDeviceProvider
import com.malinskiy.marathon.ios.IOSTestParser
import org.koin.dsl.module

val iosModule = module {
    single<DeviceProvider?> { IOSDeviceProvider(get(), get()) }
    single<TestParser?> { IOSTestParser() }
}