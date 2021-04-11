package com.malinskiy.marathon.cli.schema

import com.malinskiy.marathon.cli.schema.strategies.BatchingStrategy
import com.malinskiy.marathon.cli.schema.strategies.FlakinessStrategy
import com.malinskiy.marathon.cli.schema.strategies.PoolingStrategy
import com.malinskiy.marathon.cli.schema.strategies.RetryStrategy
import com.malinskiy.marathon.cli.schema.strategies.ShardingStrategy
import com.malinskiy.marathon.cli.schema.strategies.SortingStrategy
import java.io.File
import java.io.Serializable

val DEFAULT_ANALYTICS_CONFIGURATION = AnalyticsConfiguration.Disabled
val DEFAULT_POOLING_STRATEGY: PoolingStrategy = PoolingStrategy.Omni
val DEFAULT_SHARDING_STRATEGY: ShardingStrategy = ShardingStrategy.Disabled
val DEFAULT_SORTING_STRATEGY: SortingStrategy = SortingStrategy.Disabled
val DEFAULT_BATCHING_STRATEGY: BatchingStrategy = BatchingStrategy.Disabled
val DEFAULT_FLAKINESS_STRATEGY: FlakinessStrategy = FlakinessStrategy.Disabled
val DEFAULT_RETRY_STRATEGY: RetryStrategy = RetryStrategy.Disabled
val DEFAULT_FILTERING_CONFIGURATION: FilteringConfiguration = FilteringConfiguration(emptyList(), emptyList())


const val DEFAULT_IGNORE_FAILURES = false
const val DEFAULT_IS_CODE_COVERAGE_ENABLED = false
const val DEFAULT_FALLBACK_TO_SCREENSHOTS = false
const val DEFAULT_STRICT_MODE = false
const val DEFAULT_UNCOMPLETED_TEST_RETRY_QUOTA = Integer.MAX_VALUE
val DEFAULT_INCLUDES_SERIAL_REGEXES = emptyList<String>()
val DEFAULT_EXCLUDES_SERIAL_REGEXES = emptyList<String>()
val DEFAULT_TEST_CLASS_REGEXES = listOf("^((?!Abstract).)*Test[s]*$")
const val DEFAULT_EXECUTION_TIMEOUT_MILLIS: Long = 900_000
const val DEFAULT_OUTPUT_TIMEOUT_MILLIS: Long = 60_000
const val DEFAULT_DEBUG = true
const val DEFAULT_ANALYTICS_TRACKING = false
const val DEFAULT_DEVICE_INITIALIZATION_TIMEOUT_MILLIS = 180_000L

data class Configuration(
    val name: String,
    val outputDir: File,

    val analyticsConfiguration: AnalyticsConfiguration = DEFAULT_ANALYTICS_CONFIGURATION,
    val poolingStrategy: PoolingStrategy = DEFAULT_POOLING_STRATEGY,
    val shardingStrategy: ShardingStrategy = DEFAULT_SHARDING_STRATEGY,
    val sortingStrategy: SortingStrategy = DEFAULT_SORTING_STRATEGY,
    val batchingStrategy: BatchingStrategy = DEFAULT_BATCHING_STRATEGY,
    val flakinessStrategy: FlakinessStrategy = DEFAULT_FLAKINESS_STRATEGY,
    val retryStrategy: RetryStrategy = DEFAULT_RETRY_STRATEGY,
    val filteringConfiguration: FilteringConfiguration = DEFAULT_FILTERING_CONFIGURATION,

    val ignoreFailures: Boolean = DEFAULT_IGNORE_FAILURES,
    val isCodeCoverageEnabled: Boolean = DEFAULT_IS_CODE_COVERAGE_ENABLED,
    val fallbackToScreenshots: Boolean = DEFAULT_FALLBACK_TO_SCREENSHOTS,
    val strictMode: Boolean = DEFAULT_STRICT_MODE,
    val uncompletedTestRetryQuota: Int = DEFAULT_UNCOMPLETED_TEST_RETRY_QUOTA,

    val testClassRegexes: List<String> = DEFAULT_TEST_CLASS_REGEXES,
    val includeSerialRegexes: List<String> = DEFAULT_INCLUDES_SERIAL_REGEXES,
    val excludeSerialRegexes: List<String> = DEFAULT_EXCLUDES_SERIAL_REGEXES,

    val testBatchTimeoutMillis: Long = DEFAULT_EXECUTION_TIMEOUT_MILLIS,
    val testOutputTimeoutMillis: Long = DEFAULT_OUTPUT_TIMEOUT_MILLIS,
    val debug: Boolean = DEFAULT_DEBUG,

    val screenRecordingPolicy: ScreenRecordingPolicy = ScreenRecordingPolicy.ON_FAILURE,

    val vendorConfiguration: VendorConfiguration,

    val analyticsTracking: Boolean = DEFAULT_ANALYTICS_TRACKING,
    val deviceInitializationTimeoutMillis: Long = DEFAULT_DEVICE_INITIALIZATION_TIMEOUT_MILLIS
) : Serializable
