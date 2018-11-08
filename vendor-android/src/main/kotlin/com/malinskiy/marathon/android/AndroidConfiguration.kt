package com.malinskiy.marathon.android

import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.vendor.VendorConfiguration
import java.io.File

data class AndroidConfiguration(val androidSdk: File,
                                val applicationOutput: File?,
                                val testApplicationOutput: File,
                                val autoGrantPermission: Boolean = false,
                                val adbInitTimeoutMillis: Int = 30_000) : VendorConfiguration {

    override fun testParser(): TestParser? {
        return AndroidTestParser()
    }

    override fun deviceProvider(): DeviceProvider? {
        return AndroidDeviceProvider()
    }
}
