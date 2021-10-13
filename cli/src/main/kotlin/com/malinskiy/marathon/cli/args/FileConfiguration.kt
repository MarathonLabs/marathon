package com.malinskiy.marathon.cli.args

import com.malinskiy.marathon.config.AnalyticsConfiguration
import com.malinskiy.marathon.config.FilteringConfiguration
import com.malinskiy.marathon.config.ScreenRecordingPolicy
import com.malinskiy.marathon.config.strategy.BatchingStrategyConfiguration
import com.malinskiy.marathon.config.strategy.FlakinessStrategyConfiguration
import com.malinskiy.marathon.config.strategy.PoolingStrategyConfiguration
import com.malinskiy.marathon.config.strategy.RetryStrategyConfiguration
import com.malinskiy.marathon.config.strategy.ShardingStrategyConfiguration
import com.malinskiy.marathon.config.strategy.SortingStrategyConfiguration
import java.io.File

data class FileConfiguration(
    var name: String,
    var outputDir: File,

    var analyticsConfiguration: AnalyticsConfiguration?,
    var poolingStrategy: PoolingStrategyConfiguration?,
    var shardingStrategy: ShardingStrategyConfiguration?,
    var sortingStrategy: SortingStrategyConfiguration?,
    var batchingStrategy: BatchingStrategyConfiguration?,
    var flakinessStrategy: FlakinessStrategyConfiguration?,
    var retryStrategy: RetryStrategyConfiguration?,
    var filteringConfiguration: FilteringConfiguration?,

    var ignoreFailures: Boolean?,
    var isCodeCoverageEnabled: Boolean?,
    var fallbackToScreenshots: Boolean?,
    var strictMode: Boolean?,
    var uncompletedTestRetryQuota: Int?,

    var testClassRegexes: Collection<Regex>?,
    var includeSerialRegexes: Collection<Regex>?,
    var excludeSerialRegexes: Collection<Regex>?,

    var testBatchTimeoutMillis: Long?,
    var testOutputTimeoutMillis: Long?,
    var debug: Boolean?,

    val screenRecordingPolicy: ScreenRecordingPolicy?,

    var vendorConfiguration: FileVendorConfiguration?,

    var analyticsTracking: Boolean?,
    var deviceInitializationTimeoutMillis: Long?
)
