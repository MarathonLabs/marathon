package com.malinskiy.marathon.ios

import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.ios.di.iosModule
import com.malinskiy.marathon.log.MarathonLogConfigurator
import com.malinskiy.marathon.vendor.VendorConfiguration
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.io.File

data class IOSConfiguration(
    val derivedDataDir: File,
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
    val devicesFile: File? = null,
    val sourceRoot: File = File(".")
) : VendorConfiguration, KoinComponent {

    override fun testParser(): TestParser = get()

    override fun deviceProvider(): DeviceProvider = get()

    override fun logConfigurator(): MarathonLogConfigurator = IOSLogConfigurator()

    override fun modules() = listOf(iosModule)
}

