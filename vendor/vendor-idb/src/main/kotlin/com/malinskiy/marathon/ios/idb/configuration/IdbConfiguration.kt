package com.malinskiy.marathon.ios.idb.configuration

import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.ios.idb.IOSDeviceProvider
import com.malinskiy.marathon.ios.idb.IdbTestParser
import com.malinskiy.marathon.log.MarathonLogConfigurator
import com.malinskiy.marathon.vendor.VendorConfiguration
import java.io.File

data class IdbConfiguration(
    val idbHosts: File,
    val app: File,
    val runner: File,
    val xcTestRunPath: File
) : VendorConfiguration {
    override fun logConfigurator(): MarathonLogConfigurator {
        return IdbLogConfigurator()
    }

    override fun testParser(): TestParser {
        return IdbTestParser()
    }

    override fun deviceProvider(): DeviceProvider {
        return IOSDeviceProvider()
    }

    override fun preferableRecorderType(): DeviceFeature {
        return DeviceFeature.VIDEO
    }
}
