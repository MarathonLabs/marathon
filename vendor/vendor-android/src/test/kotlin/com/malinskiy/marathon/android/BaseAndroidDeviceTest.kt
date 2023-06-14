package com.malinskiy.marathon.android

import assertk.assertThat
import assertk.assertions.containsOnly
import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.server.junit5.AdbClient
import com.malinskiy.adam.server.junit5.AdbServer
import com.malinskiy.adam.server.junit5.AdbTest
import com.malinskiy.adam.server.stub.AndroidDebugBridgeServer
import com.malinskiy.marathon.android.adam.TestConfigurationFactory
import com.malinskiy.marathon.android.adam.TestDeviceFactory
import com.malinskiy.marathon.android.adam.boot
import com.malinskiy.marathon.android.adam.features
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.android.AggregationMode
import com.malinskiy.marathon.config.vendor.android.AllureConfiguration
import com.malinskiy.marathon.config.vendor.android.FileSyncEntry
import com.malinskiy.marathon.config.vendor.android.PathRoot
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

@AdbTest
class BaseAndroidDeviceTest {
    @AdbClient
    lateinit var client: AndroidDebugBridgeClient

    @AdbServer
    lateinit var server: AndroidDebugBridgeServer

    @Test
    fun testAllureConfiguration() {
        val configuration = TestConfigurationFactory.create(
            allureConfiguration = AllureConfiguration(enabled = true, relativeResultsDirectory = "/allure-results")
        )
        val device = TestDeviceFactory.create(client, configuration, mock())

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot()
                }
                features("emulator-5554")
            }

            device.setup()

            assertThat((configuration.vendorConfiguration as VendorConfiguration.AndroidConfiguration).fileSyncConfiguration.pull).containsOnly(
                FileSyncEntry("/allure-results", aggregationMode = AggregationMode.TEST_RUN, pathRoot = PathRoot.APP_DATA)
            )
        }
    }

    @Test
    fun testCodecovConfiguration() {
        val configuration = TestConfigurationFactory.create(
            isCodeCoverageEnabled = true
        )
        val device = TestDeviceFactory.create(client, configuration, mock())

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot()
                }
                features("emulator-5554")
            }

            device.setup()

            assertThat((configuration.vendorConfiguration as VendorConfiguration.AndroidConfiguration).fileSyncConfiguration.pull).containsOnly(
                FileSyncEntry("coverage", pathRoot = PathRoot.APP_DATA, aggregationMode = AggregationMode.POOL)
            )
        }
    }
}
