package com.malinskiy.marathon.ios

import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.log.MarathonLogConfigurator
import org.koin.dsl.module

val IOSVendor = module {
    single<DeviceProvider?> { IOSDeviceProvider(get(), get(), get(), get()) }
    single<TestParser?> { IOSTestParser(get()) }
    single<MarathonLogConfigurator> { IOSLogConfigurator(get()) }
}
