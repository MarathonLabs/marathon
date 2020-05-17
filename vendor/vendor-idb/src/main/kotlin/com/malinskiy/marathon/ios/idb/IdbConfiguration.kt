package com.malinskiy.marathon.ios.idb

import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.log.MarathonLogConfigurator
import com.malinskiy.marathon.vendor.VendorConfiguration
import java.io.File

data class IdbConfiguration(
    val idbHosts: File,
    val app: File,
    val runner: File,
    val xctestrunPath: File
) : VendorConfiguration {
    override fun logConfigurator(): MarathonLogConfigurator {
        return IdbLogConfigurator()
    }

    override fun testParser(): TestParser {
        TODO("Not yet implemented")
    }

    override fun deviceProvider(): DeviceProvider {
        return IOSDeviceProvider()
    }

    override fun preferableRecorderType(): DeviceFeature {
        return DeviceFeature.VIDEO
    }
}
