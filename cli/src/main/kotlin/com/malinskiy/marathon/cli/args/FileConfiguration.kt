package com.malinskiy.marathon.cli.args

import com.malinskiy.marathon.execution.AnalyticsConfiguration
import com.malinskiy.marathon.execution.FilteringConfiguration
import com.malinskiy.marathon.execution.strategy.*
import com.malinskiy.marathon.vendor.VendorConfiguration
import java.io.File

data class FileConfiguration(
        var name: String,
        var outputDir: File,
        var applicationOutput: File,
        var testApplicationOutput: File,

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

        var testClassRegexes: Collection<Regex>?,
        var includeSerialRegexes: Collection<Regex>?,
        var excludeSerialRegexes: Collection<Regex>?,

        var testOutputTimeoutMillis: Int?,
        var debug: Boolean?,

        val testPackage: String?,
        var autoGrantPermission: Boolean?
)