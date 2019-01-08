package com.malinskiy.marathon.android

import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.log.MarathonLogConfigurator
import com.malinskiy.marathon.vendor.VendorConfiguration
import java.io.File

const val defaultInitTimeoutMillis = 30_000

data class AndroidConfiguration(val androidSdk: File,
                                val applicationOutput: File?,
                                val testApplicationOutput: File,
                                val autoGrantPermission: Boolean = false,
                                val adbInitTimeoutMillis: Int = defaultInitTimeoutMillis) : VendorConfiguration {

    override fun testParser(): TestParser? {
        return AndroidTestParser()
    }

    override fun deviceProvider(): DeviceProvider? {
        return AndroidDeviceProvider()
    }

    override fun logConfigurator(): MarathonLogConfigurator? = null
}
