package com.malinskiy.marathon.test

import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.vendor.VendorConfiguration

class TestVendorConfiguration(var testParser: TestParser, var deviceProvider: StubDeviceProvider) : VendorConfiguration {
    override fun testParser() = testParser
    override fun deviceProvider() = deviceProvider
    override fun logConfigurator() = null
    override fun preferableRecorderType(): DeviceFeature? = null
}
