package com.malinskiy.marathon.execution.progress

import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.test.StubDevice
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.io.File
import com.malinskiy.marathon.test.Test as MarathonTest

class ProgressReporterTest {
    private val reporter = ProgressReporter(
        Configuration.Builder(
            name = "",
            outputDir = File(""),
        ).apply {
            vendorConfiguration = VendorConfiguration.StubVendorConfiguration
            debug = false
            analyticsTracking = false
        }.build()
    )
    private val deviceInfo = StubDevice().toDeviceInfo()

    
}
