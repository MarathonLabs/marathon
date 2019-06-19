package com.malinskiy.marathon.ios

import com.malinskiy.marathon.device.DeviceFeature
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
                            val alwaysEraseSimulators: Boolean,
                            val hideRunnerOutput: Boolean = false,
                            val compactOutput: Boolean = false,
                            val keepAliveIntervalMillis: Long = 0L,
                            val deviceInitializationTimeoutMillis: Long = DEFAULT_DEVICE_INITIALIZATION_TIMEOUT_MILLIS,
                            val devicesFile: File? = null,
                            val sourceRoot: File = File("."),
                            val sourceTargetName: String?,
                            val binaryParserDockerImageName: String?) : VendorConfiguration {

    companion object {
        const val DEFAULT_DEVICE_INITIALIZATION_TIMEOUT_MILLIS: Long = 300_000
    }

    override fun testParser(): TestParser? = IOSTestParser()

    override fun deviceProvider(): DeviceProvider? = IOSDeviceProvider()

    override fun logConfigurator(): MarathonLogConfigurator? = IOSLogConfigurator()

    override fun preferableRecorderType(): DeviceFeature? = null
}

