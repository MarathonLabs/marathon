package com.malinskiy.marathon.ios

import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.log.MarathonLogConfigurator
import com.malinskiy.marathon.vendor.VendorConfiguration
import java.io.File

data class IOSConfiguration(val derivedDataDir: File,
                            val xctestrunPath: File,
                            val remoteUsername: String,
                            val remotePrivateKey: File,
                            val knownHostsPath: File?,
                            val remoteRsyncPath: String,
                            val debugSsh: Boolean,
                            val hideRunnerOutput: Boolean = false,
                            val devicesFile: File? = null,
                            val sourceRoot: File = File(".")) : VendorConfiguration {

    override fun testParser(): TestParser? = IOSTestParser()

    override fun deviceProvider(): DeviceProvider? = IOSDeviceProvider()

    override fun logConfigurator(): MarathonLogConfigurator? = IOSLogConfigurator()
}

