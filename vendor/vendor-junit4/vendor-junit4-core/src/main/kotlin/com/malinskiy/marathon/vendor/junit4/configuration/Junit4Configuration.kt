package com.malinskiy.marathon.vendor.junit4.configuration

import com.malinskiy.marathon.execution.bundle.TestBundleIdentifier
import com.malinskiy.marathon.log.MarathonLogConfigurator
import com.malinskiy.marathon.vendor.VendorConfiguration
import com.malinskiy.marathon.vendor.junit4.Junit4DeviceProvider
import com.malinskiy.marathon.vendor.junit4.Junit4TestBundleIdentifier
import com.malinskiy.marathon.vendor.junit4.configuration.executor.ExecutorConfiguration
import com.malinskiy.marathon.vendor.junit4.model.JUnit4TestBundle
import com.malinskiy.marathon.vendor.junit4.parsing.RemoteJupiterTestParser
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.dsl.module
import java.io.File

/**
 * @param useArgfiles some JDK version do not support argfiles. Defaults to true
 */
data class Junit4Configuration(
    val applicationClasspath: List<File>?,
    val testClasspath: List<File>?,
    val testBundles: List<JUnit4TestBundle>?,
    val testPackageRoot: String? = null,
    val debugBooter: Boolean = false,
    val forkEvery: Int = 1000,
    val executorConfiguration: ExecutorConfiguration,
) : VendorConfiguration, KoinComponent {
    override fun logConfigurator(): MarathonLogConfigurator = Junit4LogConfigurator()

    override fun testParser() = RemoteJupiterTestParser(get())

    override fun deviceProvider() = Junit4DeviceProvider(get(), get())

    override fun testBundleIdentifier(): TestBundleIdentifier = get()

    override fun modules() = listOf(
        module {
            val testBundleIdentifier = Junit4TestBundleIdentifier()
            single<TestBundleIdentifier?> { testBundleIdentifier }
            single { testBundleIdentifier }
        }
    )

    fun testBundlesCompat(): List<JUnit4TestBundle> {
        return mutableListOf<JUnit4TestBundle>().apply {
            testBundles?.let { addAll(it) }
            if (!testClasspath.isNullOrEmpty()) {
                add(
                    JUnit4TestBundle(
                        id = "main",
                        applicationClasspath = applicationClasspath,
                        testClasspath = testClasspath,
                    )
                )
            }
        }.toList()
    }
}
