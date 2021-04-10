package com.malinskiy.marathon.cli.schema

import com.malinskiy.marathon.cli.schema.strategies.BatchingStrategy
import com.malinskiy.marathon.cli.schema.strategies.FlakinessStrategy
import com.malinskiy.marathon.cli.schema.strategies.PoolingStrategy
import com.malinskiy.marathon.cli.schema.strategies.RetryStrategy
import com.malinskiy.marathon.cli.schema.strategies.ShardingStrategy
import com.malinskiy.marathon.cli.schema.strategies.SortingStrategy
import java.io.File

private const val DEFAULT_EXECUTION_TIMEOUT_MILLIS: Long = 900_000
private const val DEFAULT_OUTPUT_TIMEOUT_MILLIS: Long = 60_000
private const val DEFAULT_DEVICE_INITIALIZATION_TIMEOUT_MILLIS = 180_000L

data class Configuration(
    val name: String,
    val outputDir: File,

    val analyticsConfiguration: AnalyticsConfiguration = AnalyticsConfiguration.Disabled,
    val poolingStrategy: PoolingStrategy = PoolingStrategy.Omni,
    val shardingStrategy: ShardingStrategy = ShardingStrategy.Disabled,
    val sortingStrategy: SortingStrategy = SortingStrategy.Disabled,
    val batchingStrategy: BatchingStrategy = BatchingStrategy.Disabled,
    val flakinessStrategy: FlakinessStrategy = FlakinessStrategy.Disabled,
    val retryStrategy: RetryStrategy = RetryStrategy.Disabled,
    val filteringConfiguration: FilteringConfiguration = FilteringConfiguration(emptyList(), emptyList()),

    val ignoreFailures: Boolean = false,
    val isCodeCoverageEnabled: Boolean = false,
    val fallbackToScreenshots: Boolean = false,
    val strictMode: Boolean = false,
    val uncompletedTestRetryQuota: Int = Integer.MAX_VALUE,

    val testClassRegexes: List<Regex> = listOf(Regex("^((?!Abstract).)*Test[s]*$")),
    val includeSerialRegexes: List<Regex> = emptyList(),
    val excludeSerialRegexes: List<Regex> = emptyList(),

    val testBatchTimeoutMillis: Long = DEFAULT_EXECUTION_TIMEOUT_MILLIS,
    val testOutputTimeoutMillis: Long = DEFAULT_OUTPUT_TIMEOUT_MILLIS,
    val debug: Boolean = true,

    val screenRecordingPolicy: ScreenRecordingPolicy = ScreenRecordingPolicy.ON_FAILURE,

    val vendorConfiguration: VendorConfiguration,

    val analyticsTracking: Boolean = false,
    val deviceInitializationTimeoutMillis: Long = DEFAULT_DEVICE_INITIALIZATION_TIMEOUT_MILLIS
)
