package com.malinskiy.marathon

import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.execution.bundle.TestBundleIdentifier
import com.malinskiy.marathon.log.MarathonLogConfigurator
import org.koin.core.module.Module

interface Vendor {
    fun logConfigurator(): MarathonLogConfigurator?
    fun testParser(): TestParser?
    fun deviceProvider(): DeviceProvider
    fun testBundleIdentifier(): TestBundleIdentifier? = null

    fun modules(): List<Module> = emptyList()
}
