package com.malinskiy.marathon.vendor

import com.malinskiy.marathon.cache.test.key.ComponentCacheKeyProvider
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.ComponentInfoExtractor
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.log.MarathonLogConfigurator
import org.koin.core.module.Module

interface VendorConfiguration {
    fun logConfigurator(): MarathonLogConfigurator?
    fun testParser(): TestParser?
    fun deviceProvider(): DeviceProvider?
    fun preferableRecorderType(): DeviceFeature?
    fun componentInfoExtractor(): ComponentInfoExtractor?
    fun componentCacheKeyProvider(): ComponentCacheKeyProvider?

    fun modules(): List<Module> = emptyList()
}
