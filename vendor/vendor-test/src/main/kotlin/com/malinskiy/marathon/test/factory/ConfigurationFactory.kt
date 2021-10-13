package com.malinskiy.marathon.test.factory

import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.ScreenRecordingPolicy
import com.malinskiy.marathon.config.strategy.FlakinessStrategyConfiguration
import com.malinskiy.marathon.config.strategy.RetryStrategyConfiguration
import com.malinskiy.marathon.config.strategy.ShardingStrategyConfiguration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.test.StubDeviceProvider
import com.malinskiy.marathon.test.Test
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.channels.Channel
import java.nio.file.Files

class ConfigurationFactory(val testParser: TestParser, val deviceProvider: StubDeviceProvider) {
    var name = "DEFAULT_TEST_CONFIG"
    var outputDir = Files.createTempDirectory("test-run").toFile()
    var vendorConfiguration = VendorConfiguration.StubVendorConfiguration
    var debug = null
    var batchingStrategy = null
    var analyticsConfiguration = null
    var excludeSerialRegexes: List<Regex>? = null
    var fallbackToScreenshots = null
    var strictMode = null
    var uncompletedTestRetryQuota: Int? = null
    var filteringConfiguration = null
    var flakinessStrategy: FlakinessStrategyConfiguration? = null
    var ignoreFailures = null
    var includeSerialRegexes: List<Regex>? = null
    var isCodeCoverageEnabled = null
    var poolingStrategy = null
    var retryStrategy: RetryStrategyConfiguration? = null
    var shardingStrategy: ShardingStrategyConfiguration? = null
    var sortingStrategy = null
    var testClassRegexes = null
    var testBatchTimeoutMillis = null
    var testOutputTimeoutMillis = null
    var analyticsTracking = false
    var screenRecordingPolicy: ScreenRecordingPolicy? = null
    var deviceInitializationTimeoutMillis: Long? = null

    fun tests(block: () -> List<Test>) {
        whenever(testParser.extract()).thenReturn(block.invoke())
    }

    fun devices(f: suspend (Channel<DeviceProvider.DeviceEvent>) -> Unit) {
        deviceProvider.providingLogic = f
    }

    fun build(): Configuration =
        Configuration(
            name,
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
            strictMode,
            uncompletedTestRetryQuota,
            testClassRegexes,
            includeSerialRegexes,
            excludeSerialRegexes,
            testBatchTimeoutMillis,
            testOutputTimeoutMillis,
            debug,
            screenRecordingPolicy,
            vendorConfiguration,
            analyticsTracking,
            deviceInitializationTimeoutMillis
        )
}
