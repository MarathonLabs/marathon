package com.malinskiy.marathon.test

import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.vendor.VendorConfiguration

class TestVendorConfiguration(var testParser: TestParser, var deviceProvider: StubDeviceProvider) : VendorConfiguration {
    override fun testParser() = testParser
    override fun deviceProvider() = deviceProvider
}