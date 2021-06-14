package com.malinskiy.marathon.vendor.junit4.configuration

import com.malinskiy.marathon.log.MarathonLogConfigurator
import com.malinskiy.marathon.vendor.VendorConfiguration
import com.malinskiy.marathon.vendor.junit4.Junit4DeviceProvider
import com.malinskiy.marathon.vendor.junit4.JupiterTestParser
import com.malinskiy.marathon.vendor.junit4.parsing.TestBundle
import org.koin.core.KoinComponent
import org.koin.core.get
import java.io.File

data class Junit4Configuration(
    val applicationClasspath: List<File>?,
    val testClasspath: List<File>?,
    val testBundles: List<TestBundle>?,
    val testPackageRoot: String? = null,
    val debugBooter: Boolean = false,
) : VendorConfiguration, KoinComponent {
    override fun logConfigurator(): MarathonLogConfigurator = Junit4LogConfigurator()

    override fun testParser() = JupiterTestParser()

    override fun deviceProvider() = Junit4DeviceProvider(get())
}
