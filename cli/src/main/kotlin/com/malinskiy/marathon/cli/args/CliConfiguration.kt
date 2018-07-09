package com.malinskiy.marathon.cli.args

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.malinskiy.marathon.execution.AnalyticsConfiguration
import com.malinskiy.marathon.execution.FilteringConfiguration
import com.malinskiy.marathon.execution.strategy.*
import com.malinskiy.marathon.vendor.VendorConfiguration
import java.io.File

data class CliConfiguration(
        var name: String,
        var outputDir: File,
        var applicationOutput: File,
        var testApplicationOutput: File,

//        var analyticsConfiguration: AnalyticsConfiguration,
        var poolingStrategy: PoolingStrategy,
        var shardingStrategy: ShardingStrategy,
        var sortingStrategy: SortingStrategy,
        var batchingStrategy: BatchingStrategy,
        var flakinessStrategy: FlakinessStrategy,
        var retryStrategy: RetryStrategy,
        var filteringConfiguration: FilteringConfiguration
//
//        var ignoreFailures: Boolean,
//        var isCodeCoverageEnabled: Boolean,
//        var fallbackToScreenshots: Boolean,
//
//        var testClassRegexes: Collection<Regex>,
//        var includedTestAnnotations: Collection<String>,
//        var excludedTestAnnotations: Collection<String>,
//        var includeSerialRegexes: Collection<Regex>,
//        var excludeSerialRegexes: Collection<Regex>,
//
//        var testOutputTimeoutMillis: Int,
//        var debug: Boolean,
//
//        var testPackage: String?,
//        var autoGrantPermission: Boolean,
//        var vendorConfiguration: VendorConfiguration
)