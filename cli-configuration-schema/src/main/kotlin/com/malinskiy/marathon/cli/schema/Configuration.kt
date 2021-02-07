package com.malinskiy.marathon.cli.schema

import com.malinskiy.marathon.cli.schema.strategies.BatchingStrategy
import com.malinskiy.marathon.cli.schema.strategies.FlakinessStrategy
import com.malinskiy.marathon.cli.schema.strategies.PoolingStrategy
import com.malinskiy.marathon.cli.schema.strategies.RetryStrategy
import com.malinskiy.marathon.cli.schema.strategies.ShardingStrategy
import com.malinskiy.marathon.cli.schema.strategies.SortingStrategy
import java.io.File

data class Configuration(
    val name: String,
    val outputDir: File,

    val analyticsConfiguration: AnalyticsConfiguration,
    val poolingStrategy: PoolingStrategy,
    val shardingStrategy: ShardingStrategy,
    val sortingStrategy: SortingStrategy,
    val batchingStrategy: BatchingStrategy,
    val flakinessStrategy: FlakinessStrategy,
    val retryStrategy: RetryStrategy,
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
)
