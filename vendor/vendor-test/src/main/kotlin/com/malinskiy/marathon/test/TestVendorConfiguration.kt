package com.malinskiy.marathon.test

import com.malinskiy.marathon.cache.test.key.ComponentCacheKeyProvider
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.vendor.VendorConfiguration
import org.koin.core.module.Module
import org.koin.dsl.module

class TestVendorConfiguration(
    var testParser: TestParser,
    var deviceProvider: StubDeviceProvider,
    var componentInfoExtractor: StubComponentInfoExtractor,
    var componentCacheKeyProvider: StubComponentCacheKeyProvider
) : VendorConfiguration {
    override fun testParser() = testParser
    override fun componentInfoExtractor() = componentInfoExtractor
    override fun deviceProvider() = deviceProvider
    override fun componentCacheKeyProvider(): ComponentCacheKeyProvider? = componentCacheKeyProvider
    override fun logConfigurator() = null
    override fun preferableRecorderType(): DeviceFeature? = null

    override fun modules(): List<Module> =
        listOf(module {
            single<ComponentCacheKeyProvider?> { componentCacheKeyProvider }
        })
}
