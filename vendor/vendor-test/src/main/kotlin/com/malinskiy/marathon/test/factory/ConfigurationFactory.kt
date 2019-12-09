package com.malinskiy.marathon.test.factory

import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.CacheConfiguration
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.strategy.FlakinessStrategy
import com.malinskiy.marathon.execution.strategy.ShardingStrategy
import com.malinskiy.marathon.test.Mocks
import com.malinskiy.marathon.test.StubComponentCacheKeyProvider
import com.malinskiy.marathon.test.StubComponentInfoExtractor
import com.malinskiy.marathon.test.StubDeviceProvider
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestVendorConfiguration
import kotlinx.coroutines.channels.Channel
import org.amshove.kluent.When
import org.amshove.kluent.`it returns`
import org.amshove.kluent.any
import org.amshove.kluent.calling
import java.nio.file.Files

class ConfigurationFactory {
    var name = "DEFAULT_TEST_CONFIG"
    var outputDir = Files.createTempDirectory("test-run").toFile()
    var vendorConfiguration = TestVendorConfiguration(
        Mocks.TestParser.DEFAULT,
        StubDeviceProvider(),
        StubComponentInfoExtractor(),
        StubComponentCacheKeyProvider()
    )
    var debug = null
    var batchingStrategy = null
    var customAnalyticsTracker = null
    var analyticsConfiguration = null
    var excludeSerialRegexes: List<Regex>? = null
    var fallbackToScreenshots = null
    var strictMode = null
    var uncompletedTestRetryQuota: Int? = null
    var filteringConfiguration = null
    var flakinessStrategy: FlakinessStrategy? = null
    var cache: CacheConfiguration? = null
    var ignoreFailures = null
    var includeSerialRegexes: List<Regex>? = null
    var isCodeCoverageEnabled = null
    var poolingStrategy = null
    var retryStrategy = null
    var shardingStrategy: ShardingStrategy? = null
    var sortingStrategy = null
    var testClassRegexes = null
    var testBatchTimeoutMillis = null
    var testOutputTimeoutMillis = null
    var analyticsTracking = false

    fun tests(block: () -> List<Test>) {
        val testParser = vendorConfiguration.testParser()!!
        When calling testParser.extract(any()) `it returns` (block.invoke())
    }

    fun devices(f: suspend (Channel<DeviceProvider.DeviceEvent>) -> Unit) {
        val stubDeviceProvider = vendorConfiguration.deviceProvider() as StubDeviceProvider
        stubDeviceProvider.providingLogic = f
    }

    fun build(): Configuration =
        Configuration(
            name,
            outputDir,
            analyticsConfiguration,
            customAnalyticsTracker,
            poolingStrategy,
            shardingStrategy,
            sortingStrategy,
            batchingStrategy,
            flakinessStrategy,
            retryStrategy,
            filteringConfiguration,
            cache,
            ignoreFailures,
            isCodeCoverageEnabled,
            fallbackToScreenshots,
            strictMode,
            uncompletedTestRetryQuota,
            testClassRegexes,
            includeSerialRegexes,
            excludeSerialRegexes,
            testBatchTimeoutMillis,
            testOutputTimeoutMillis,
            debug,
            vendorConfiguration,
            analyticsTracking
        )
}
