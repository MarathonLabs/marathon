package com.malinskiy.marathon.config

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

data class Configuration constructor(
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
    val deviceInitializationTimeoutMillis: Long
) {

    constructor(
        name: String,
        outputDir: File,

        analyticsConfiguration: AnalyticsConfiguration?,
        poolingStrategy: PoolingStrategyConfiguration?,
        shardingStrategy: ShardingStrategyConfiguration?,
        sortingStrategy: SortingStrategyConfiguration?,
        batchingStrategy: BatchingStrategyConfiguration?,
        flakinessStrategy: FlakinessStrategyConfiguration?,
        retryStrategy: RetryStrategyConfiguration?,
        filteringConfiguration: FilteringConfiguration?,

        ignoreFailures: Boolean?,
        isCodeCoverageEnabled: Boolean?,
        fallbackToScreenshots: Boolean?,
        strictMode: Boolean?,
        uncompletedTestRetryQuota: Int?,

        testClassRegexes: Collection<Regex>?,
        includeSerialRegexes: Collection<Regex>?,
        excludeSerialRegexes: Collection<Regex>?,

        testBatchTimeoutMillis: Long?,
        testOutputTimeoutMillis: Long?,
        debug: Boolean?,

        screenRecordingPolicy: ScreenRecordingPolicy?,

        vendorConfiguration: VendorConfiguration,

        analyticsTracking: Boolean?,
        deviceInitializationTimeoutMillis: Long?
    ) :

            this(
                name = name,
                outputDir = outputDir,
                analyticsConfiguration = analyticsConfiguration ?: AnalyticsConfiguration.DisabledAnalytics,
                poolingStrategy = poolingStrategy ?: PoolingStrategyConfiguration.OmniPoolingStrategyConfiguration,
                shardingStrategy = shardingStrategy ?: ShardingStrategyConfiguration.ParallelShardingStrategyConfiguration,
                sortingStrategy = sortingStrategy ?: SortingStrategyConfiguration.NoSortingStrategyConfiguration,
                batchingStrategy = batchingStrategy ?: BatchingStrategyConfiguration.IsolateBatchingStrategyConfiguration,
                flakinessStrategy = flakinessStrategy ?: FlakinessStrategyConfiguration.IgnoreFlakinessStrategyConfiguration,
                retryStrategy = retryStrategy ?: RetryStrategyConfiguration.NoRetryStrategyConfiguration,
                filteringConfiguration = filteringConfiguration ?: FilteringConfiguration(emptyList(), emptyList()),
                ignoreFailures = ignoreFailures ?: false,
                isCodeCoverageEnabled = isCodeCoverageEnabled ?: false,
                fallbackToScreenshots = fallbackToScreenshots ?: false,
                strictMode = strictMode ?: false,
                uncompletedTestRetryQuota = uncompletedTestRetryQuota ?: Integer.MAX_VALUE,
                testClassRegexes = testClassRegexes ?: listOf(Regex("^((?!Abstract).)*Test[s]*$")),
                includeSerialRegexes = includeSerialRegexes ?: emptyList(),
                excludeSerialRegexes = excludeSerialRegexes ?: emptyList(),
                testBatchTimeoutMillis = testBatchTimeoutMillis ?: DEFAULT_BATCH_EXECUTION_TIMEOUT_MILLIS,
                testOutputTimeoutMillis = testOutputTimeoutMillis ?: DEFAULT_OUTPUT_TIMEOUT_MILLIS,
                debug = debug ?: true,
                screenRecordingPolicy = screenRecordingPolicy ?: ScreenRecordingPolicy.ON_FAILURE,
                vendorConfiguration = vendorConfiguration,
                analyticsTracking = analyticsTracking ?: false,
                deviceInitializationTimeoutMillis = deviceInitializationTimeoutMillis ?: DEFAULT_DEVICE_INITIALIZATION_TIMEOUT_MILLIS
            )

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
}
