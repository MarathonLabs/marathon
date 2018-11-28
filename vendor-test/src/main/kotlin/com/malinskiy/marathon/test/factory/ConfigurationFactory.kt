package com.malinskiy.marathon.test.factory

import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.test.Mocks
import com.malinskiy.marathon.test.StubDeviceProvider
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestVendorConfiguration
import com.malinskiy.marathon.vendor.VendorConfiguration
import kotlinx.coroutines.experimental.channels.Channel
import org.amshove.kluent.When
import org.amshove.kluent.`it returns`
import org.amshove.kluent.any
import org.amshove.kluent.calling
import java.nio.file.Files

class ConfigurationFactory {
    var name = "DEFAULT_TEST_CONFIG"
    var outputDir = Files.createTempDirectory("test-run").toFile()
    var vendorConfiguration = TestVendorConfiguration(Mocks.TestParser.DEFAULT, StubDeviceProvider())
    var debug = null
    var batchingStrategy = null
    var analyticsConfiguration = null
    var excludeSerialRegexes = null
    var fallbackToScreenshots = null
    var filteringConfiguration = null
    var flakinessStrategy = null
    var ignoreFailures = null
    var includeSerialRegexes = null
    var isCodeCoverageEnabled = null
    var poolingStrategy = null
    var retryStrategy = null
    var shardingStrategy = null
    var sortingStrategy = null
    var testClassRegexes = null
    var testOutputTimeoutMillis = null

    fun tests(block: () -> List<Test>) {
        val testParser = vendorConfiguration.testParser()!!
        When calling testParser.extract(any()) `it returns` (block.invoke())
    }

    fun devices(f: suspend (Channel<DeviceProvider.DeviceEvent>) -> Unit) {
        val stubDeviceProvider = vendorConfiguration.deviceProvider() as StubDeviceProvider
        stubDeviceProvider.providingLogic = f
    }

    fun build(): Configuration =
            Configuration(name,
                    outputDir,
                    analyticsConfiguration,
                    poolingStrategy,
                    shardingStrategy,
                    sortingStrategy,
                    batchingStrategy,
                    flakinessStrategy,
                    retryStrategy,
                    filteringConfiguration,
                    ignoreFailures,
                    isCodeCoverageEnabled,
                    fallbackToScreenshots,
                    testClassRegexes,
                    includeSerialRegexes,
                    excludeSerialRegexes,
                    testOutputTimeoutMillis,
                    debug,
                    vendorConfiguration)
}