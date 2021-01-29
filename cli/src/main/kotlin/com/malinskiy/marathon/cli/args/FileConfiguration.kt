package com.malinskiy.marathon.cli.args

import com.malinskiy.marathon.execution.AnalyticsConfiguration
import com.malinskiy.marathon.execution.FilteringConfiguration
import com.malinskiy.marathon.execution.policy.ScreenRecordingPolicy
import com.malinskiy.marathon.execution.strategy.BatchingStrategy
import com.malinskiy.marathon.execution.strategy.FlakinessStrategy
import com.malinskiy.marathon.execution.strategy.IgnoreFlakinessStrategy
import com.malinskiy.marathon.execution.strategy.IsolateBatchingStrategy
import com.malinskiy.marathon.execution.strategy.NoRetryStrategy
import com.malinskiy.marathon.execution.strategy.NoSortingStrategy
import com.malinskiy.marathon.execution.strategy.OmniPoolingStrategy
import com.malinskiy.marathon.execution.strategy.ParallelShardingStrategy
import com.malinskiy.marathon.execution.strategy.PoolingStrategy
import com.malinskiy.marathon.execution.strategy.RetryStrategy
import com.malinskiy.marathon.execution.strategy.ShardingStrategy
import com.malinskiy.marathon.execution.strategy.SortingStrategy
import java.io.File

data class FileConfiguration(
    var name: String,
    var outputDir: File,

    var analyticsConfiguration: AnalyticsConfiguration?,
    var poolingStrategy: PoolingStrategy?,
    var shardingStrategy: ShardingStrategy?,
    var sortingStrategy: SortingStrategy?,
    var batchingStrategy: BatchingStrategy?,
    var flakinessStrategy: FlakinessStrategy?,
    var retryStrategy: RetryStrategy?,
    var filteringConfiguration: FilteringConfiguration?,

    var ignoreFailures: Boolean?,
    var isCodeCoverageEnabled: Boolean?,
    var fallbackToScreenshots: Boolean?,
    var strictMode: Boolean?,
    var uncompletedTestRetryQuota: Int?,

    var testClassRegexes: List<Regex>?,
    var includeSerialRegexes: List<Regex>?,
    var excludeSerialRegexes: List<Regex>?,

    var testBatchTimeoutMillis: Long?,
    var testOutputTimeoutMillis: Long?,
    var debug: Boolean?,

    val screenRecordingPolicy: ScreenRecordingPolicy?,

    var vendorConfiguration: FileVendorConfiguration,

    var analyticsTracking: Boolean?,
    var deviceInitializationTimeoutMillis: Long?
)
