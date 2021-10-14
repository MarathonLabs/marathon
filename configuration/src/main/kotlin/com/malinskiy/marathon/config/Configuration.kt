package com.malinskiy.marathon.config

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.malinskiy.marathon.config.strategy.BatchingStrategyConfiguration
import com.malinskiy.marathon.config.strategy.FlakinessStrategyConfiguration
import com.malinskiy.marathon.config.strategy.PoolingStrategyConfiguration
import com.malinskiy.marathon.config.strategy.RetryStrategyConfiguration
import com.malinskiy.marathon.config.strategy.ShardingStrategyConfiguration
import com.malinskiy.marathon.config.strategy.SortingStrategyConfiguration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import java.io.File

private const val DEFAULT_BATCH_EXECUTION_TIMEOUT_MILLIS: Long = 1800_000 //30 min
private const val DEFAULT_OUTPUT_TIMEOUT_MILLIS: Long = 300_000 //5 min
private const val DEFAULT_DEVICE_INITIALIZATION_TIMEOUT_MILLIS = 180_000L

@JsonDeserialize(builder = Configuration.Builder::class)
data class Configuration private constructor(
    val name: String,
    val outputDir: File,

    val analyticsConfiguration: AnalyticsConfiguration,
    val poolingStrategy: PoolingStrategyConfiguration,
    val shardingStrategy: ShardingStrategyConfiguration,
    val sortingStrategy: SortingStrategyConfiguration,
    val batchingStrategy: BatchingStrategyConfiguration,
    val flakinessStrategy: FlakinessStrategyConfiguration,
    val retryStrategy: RetryStrategyConfiguration,
    val filteringConfiguration: FilteringConfiguration,

    val ignoreFailures: Boolean,
    val isCodeCoverageEnabled: Boolean,
    val fallbackToScreenshots: Boolean,
    val strictMode: Boolean,
    val uncompletedTestRetryQuota: Int,

    val testClassRegexes: Collection<Regex>,
    val includeSerialRegexes: Collection<Regex>,
    val excludeSerialRegexes: Collection<Regex>,

    val testBatchTimeoutMillis: Long,
    val testOutputTimeoutMillis: Long,
    val debug: Boolean,

    val screenRecordingPolicy: ScreenRecordingPolicy,

    val vendorConfiguration: VendorConfiguration,

    val analyticsTracking: Boolean,
    val deviceInitializationTimeoutMillis: Long,
) {
    fun toMap() =
        mapOf<String, String>(
            "name" to name,
            "outputDir" to outputDir.absolutePath,
            "analyticsConfiguration" to analyticsConfiguration.toString(),
            "pooling" to poolingStrategy.toString(),
            "sharding" to shardingStrategy.toString(),
            "sorting" to sortingStrategy.toString(),
            "batching" to batchingStrategy.toString(),
            "flakiness" to flakinessStrategy.toString(),
            "retry" to retryStrategy.toString(),
            "filtering" to filteringConfiguration.toString(),
            "ignoreFailures" to ignoreFailures.toString(),
            "isCodeCoverageEnabled" to isCodeCoverageEnabled.toString(),
            "fallbackToScreenshots" to fallbackToScreenshots.toString(),
            "strictMode" to strictMode.toString(),
            "testClassRegexes" to testClassRegexes.toString(),
            "includeSerialRegexes" to includeSerialRegexes.toString(),
            "excludeSerialRegexes" to excludeSerialRegexes.toString(),
            "testBatchTimeoutMillis" to testBatchTimeoutMillis.toString(),
            "testOutputTimeoutMillis" to testOutputTimeoutMillis.toString(),
            "debug" to debug.toString(),
            "screenRecordingPolicy" to screenRecordingPolicy.toString(),
            "vendorConfiguration" to vendorConfiguration.toString(),
            "deviceInitializationTimeoutMillis" to deviceInitializationTimeoutMillis.toString()
        )

    class Builder(
        val name: String,
        val outputDir: File,
        val vendorConfiguration: VendorConfiguration,
    ) {
        var analyticsConfiguration: AnalyticsConfiguration = AnalyticsConfiguration.DisabledAnalytics
        var poolingStrategy: PoolingStrategyConfiguration = PoolingStrategyConfiguration.OmniPoolingStrategyConfiguration
        var shardingStrategy: ShardingStrategyConfiguration = ShardingStrategyConfiguration.ParallelShardingStrategyConfiguration
        var sortingStrategy: SortingStrategyConfiguration = SortingStrategyConfiguration.NoSortingStrategyConfiguration
        var batchingStrategy: BatchingStrategyConfiguration = BatchingStrategyConfiguration.IsolateBatchingStrategyConfiguration
        var flakinessStrategy: FlakinessStrategyConfiguration = FlakinessStrategyConfiguration.IgnoreFlakinessStrategyConfiguration
        var retryStrategy: RetryStrategyConfiguration = RetryStrategyConfiguration.NoRetryStrategyConfiguration
        var filteringConfiguration: FilteringConfiguration = FilteringConfiguration(emptyList(), emptyList())

        var ignoreFailures: Boolean = false
        var isCodeCoverageEnabled: Boolean = false
        var fallbackToScreenshots: Boolean = false
        var strictMode: Boolean = false
        var uncompletedTestRetryQuota: Int = Integer.MAX_VALUE

        var testClassRegexes: Collection<Regex> = listOf(Regex("^((?!Abstract).)*Test[s]*$"))
        var includeSerialRegexes: Collection<Regex> = emptyList()
        var excludeSerialRegexes: Collection<Regex> = emptyList()

        var testBatchTimeoutMillis: Long = DEFAULT_BATCH_EXECUTION_TIMEOUT_MILLIS
        var testOutputTimeoutMillis: Long = DEFAULT_OUTPUT_TIMEOUT_MILLIS
        var debug: Boolean = true

        var screenRecordingPolicy: ScreenRecordingPolicy = ScreenRecordingPolicy.ON_FAILURE

        var analyticsTracking: Boolean = false
        var deviceInitializationTimeoutMillis: Long = DEFAULT_DEVICE_INITIALIZATION_TIMEOUT_MILLIS

        fun build(): Configuration {
            return Configuration(
                name = name,
                outputDir = outputDir,
                analyticsConfiguration = analyticsConfiguration,
                poolingStrategy = poolingStrategy,
                shardingStrategy = shardingStrategy,
                sortingStrategy = sortingStrategy,
                batchingStrategy = batchingStrategy,
                flakinessStrategy = flakinessStrategy,
                retryStrategy = retryStrategy,
                filteringConfiguration = filteringConfiguration,
                ignoreFailures = ignoreFailures,
                isCodeCoverageEnabled = isCodeCoverageEnabled,
                fallbackToScreenshots = fallbackToScreenshots,
                strictMode = strictMode,
                uncompletedTestRetryQuota = uncompletedTestRetryQuota,
                testClassRegexes = testClassRegexes,
                includeSerialRegexes = includeSerialRegexes,
                excludeSerialRegexes = excludeSerialRegexes,
                testBatchTimeoutMillis = testBatchTimeoutMillis,
                testOutputTimeoutMillis = testOutputTimeoutMillis,
                debug = debug,
                screenRecordingPolicy = screenRecordingPolicy,
                vendorConfiguration = vendorConfiguration,
                analyticsTracking = analyticsTracking,
                deviceInitializationTimeoutMillis = deviceInitializationTimeoutMillis
            )
        }
    }
}
