package com.malinskiy.marathon.cli.args

import com.malinskiy.marathon.execution.AnalyticsConfiguration
import com.malinskiy.marathon.execution.FilteringConfiguration
import com.malinskiy.marathon.execution.strategy.BatchingStrategy
import com.malinskiy.marathon.execution.strategy.FlakinessStrategy
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

    var testClassRegexes: Collection<Regex>?,
    var includeSerialRegexes: Collection<Regex>?,
    var excludeSerialRegexes: Collection<Regex>?,

    var testBatchTimeoutMillis: Long?,
    var testOutputTimeoutMillis: Long?,
    var shuffleRunOrder: Boolean?,
    var debug: Boolean?,

    var vendorConfiguration: FileVendorConfiguration?,

    var analyticsTracking: Boolean?
)
