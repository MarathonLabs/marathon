package com.malinskiy.marathon.ios

import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.vendor.VendorConfiguration
import java.io.File

data class IOSConfiguration(val xctestrunPath: File,
                            val derivedDataDir: File,
                            val remoteUsername: String,
                            val remotePublicKey: File,
                            val sourceRoot: File = File(".")) : VendorConfiguration {

    override fun testParser(): TestParser? {
        return IOSTestParser()
    }

    override fun deviceProvider(): DeviceProvider? {
        return IOSDeviceProvider()
    }
}

