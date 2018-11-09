package com.malinskiy.marathon.ios

import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.vendor.VendorConfiguration
import java.io.File

data class IOSConfiguration(val derivedDataDir: File,
                            val xctestrunPath: File,
                            val remoteUsername: String,
                            val remotePrivateKey: File,
                            val remoteRsyncPath: String,
                            val debugSsh: Boolean,
                            val devicesFile: File? = null,
                            val sourceRoot: File = File(".")) : VendorConfiguration {

    override fun testParser(): TestParser? {
        return IOSTestParser()
    }

    override fun deviceProvider(): DeviceProvider? {
        return IOSDeviceProvider()
    }
}

