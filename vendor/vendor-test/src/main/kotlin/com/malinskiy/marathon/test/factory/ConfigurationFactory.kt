package com.malinskiy.marathon.test.factory

import com.malinskiy.marathon.config.AnalyticsConfiguration
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.FilteringConfiguration
import com.malinskiy.marathon.config.ScreenRecordingPolicy
import com.malinskiy.marathon.config.strategy.BatchingStrategyConfiguration
import com.malinskiy.marathon.config.strategy.FlakinessStrategyConfiguration
import com.malinskiy.marathon.config.strategy.PoolingStrategyConfiguration
import com.malinskiy.marathon.config.strategy.RetryStrategyConfiguration
import com.malinskiy.marathon.config.strategy.ShardingStrategyConfiguration
import com.malinskiy.marathon.config.strategy.SortingStrategyConfiguration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.LocalTestParser
import com.malinskiy.marathon.test.StubDeviceProvider
import com.malinskiy.marathon.test.Test
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.channels.Channel
import java.nio.file.Files

class ConfigurationFactory(val testParser: LocalTestParser, val deviceProvider: StubDeviceProvider) {
    var name = "DEFAULT_TEST_CONFIG"
    var outputDir = Files.createTempDirectory("test-run").toFile()
    var vendorConfiguration = VendorConfiguration.StubVendorConfiguration
    var debug = true
    var batchingStrategy: BatchingStrategyConfiguration = BatchingStrategyConfiguration.IsolateBatchingStrategyConfiguration
    var analyticsConfiguration: AnalyticsConfiguration = AnalyticsConfiguration.DisabledAnalytics
    var excludeSerialRegexes: List<Regex> = emptyList()
    var strictMode: Boolean = false
    var uncompletedTestRetryQuota: Int = Int.MAX_VALUE
    var filteringConfiguration: FilteringConfiguration = FilteringConfiguration()
    var flakinessStrategy: FlakinessStrategyConfiguration = FlakinessStrategyConfiguration.IgnoreFlakinessStrategyConfiguration
    var ignoreFailures: Boolean = false
    var includeSerialRegexes: List<Regex> = emptyList()
    var isCodeCoverageEnabled: Boolean = false
    var poolingStrategy: PoolingStrategyConfiguration = PoolingStrategyConfiguration.OmniPoolingStrategyConfiguration
    var retryStrategy: RetryStrategyConfiguration = RetryStrategyConfiguration.NoRetryStrategyConfiguration
    var shardingStrategy: ShardingStrategyConfiguration = ShardingStrategyConfiguration.ParallelShardingStrategyConfiguration
    var sortingStrategy: SortingStrategyConfiguration = SortingStrategyConfiguration.NoSortingStrategyConfiguration
    var testClassRegexes = listOf(Regex("^((?!Abstract).)*Test[s]*$"))
    var testBatchTimeoutMillis = 180_000L
    var testOutputTimeoutMillis = 60_000L
    var analyticsTracking = false
    var screenRecordingPolicy: ScreenRecordingPolicy = ScreenRecordingPolicy.ON_ANY
    var deviceInitializationTimeoutMillis = 60_000L

    suspend fun tests(block: () -> List<Test>) {
        whenever(testParser.extract()).thenReturn(block.invoke())
    }

    fun devices(f: suspend (Channel<DeviceProvider.DeviceEvent>) -> Unit) {
        deviceProvider.providingLogic = f
    }

    fun build(): Configuration = Configuration.Builder(
        name, outputDir, 
    ).apply {
        vendorConfiguration = this@ConfigurationFactory.vendorConfiguration
        analyticsConfiguration = this@ConfigurationFactory.analyticsConfiguration
        poolingStrategy = this@ConfigurationFactory.poolingStrategy
        shardingStrategy = this@ConfigurationFactory.shardingStrategy
        sortingStrategy = this@ConfigurationFactory.sortingStrategy
        batchingStrategy = this@ConfigurationFactory.batchingStrategy
        flakinessStrategy = this@ConfigurationFactory.flakinessStrategy
        retryStrategy = this@ConfigurationFactory.retryStrategy
        filteringConfiguration = this@ConfigurationFactory.filteringConfiguration
        ignoreFailures = this@ConfigurationFactory.ignoreFailures
        isCodeCoverageEnabled = this@ConfigurationFactory.isCodeCoverageEnabled
        strictMode = this@ConfigurationFactory.strictMode
        uncompletedTestRetryQuota = this@ConfigurationFactory.uncompletedTestRetryQuota
        testClassRegexes = this@ConfigurationFactory.testClassRegexes
        includeSerialRegexes = this@ConfigurationFactory.includeSerialRegexes
        excludeSerialRegexes = this@ConfigurationFactory.excludeSerialRegexes
        testBatchTimeoutMillis = this@ConfigurationFactory.testBatchTimeoutMillis
        testOutputTimeoutMillis = this@ConfigurationFactory.testOutputTimeoutMillis
        debug = this@ConfigurationFactory.debug
        screenRecordingPolicy = this@ConfigurationFactory.screenRecordingPolicy
        analyticsTracking = this@ConfigurationFactory.analyticsTracking
        deviceInitializationTimeoutMillis = this@ConfigurationFactory.deviceInitializationTimeoutMillis
    }.build()
}
