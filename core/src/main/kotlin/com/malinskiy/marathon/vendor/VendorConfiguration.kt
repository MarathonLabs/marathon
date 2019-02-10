package com.malinskiy.marathon.vendor

import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.log.MarathonLogConfigurator

interface VendorConfiguration {
    fun logConfigurator(): MarathonLogConfigurator?
    fun testParser(): TestParser?
    fun deviceProvider(): DeviceProvider?
}
