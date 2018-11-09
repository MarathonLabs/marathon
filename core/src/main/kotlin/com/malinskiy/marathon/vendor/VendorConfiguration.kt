package com.malinskiy.marathon.vendor

import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestParser

interface VendorConfiguration {
    fun testParser(): TestParser?
    fun deviceProvider(): DeviceProvider?
}
