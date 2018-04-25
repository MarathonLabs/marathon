package com.malinskiy.marathon.execution

import com.malinskiy.marathon.execution.strategy.*
import com.malinskiy.marathon.execution.strategy.impl.*
import com.malinskiy.marathon.execution.strategy.impl.pooling.OmniPoolingStrategy
import com.malinskiy.marathon.vendor.VendorConfiguration
import java.io.File

data class Configuration constructor(
        val baseOutputDir: File,
        val outputDir: File,
        val applicationOutput: File,
        val testApplicationOutput: File,

        val poolingStrategy: PoolingStrategy,
        val shardingStrategy: ShardingStrategy,
        val sortingStrategy: SortingStrategy,
        val batchingStrategy: BatchingStrategy,
        val flakinessStrategy: FlakinessStrategy,
        val retryStrategy: RetryStrategy,

        val ignoreFailures: Boolean,
        val isCodeCoverageEnabled: Boolean,
        val fallbackToScreenshots: Boolean,

        val testClassRegexes: Collection<Regex>,
        val includedTestAnnotations: Collection<String>,
        val excludedTestAnnotations: Collection<String>,
        val includeSerialRegexes: Collection<Regex>,
        val excludeSerialRegexes: Collection<Regex>,

        val testOutputTimeoutMillis: Int,

        val testPackage: String?,
        val autoGrantPermission: Boolean,
        val vendorConfiguration: VendorConfiguration) {

    constructor(baseOutputDir: File,
                outputDir: File,
                applicationOutput: File,
                testApplicationOutput: File,

                poolingStrategy: PoolingStrategy?,
                shardingStrategy: ShardingStrategy?,
                sortingStrategy: SortingStrategy?,
                batchingStrategy: BatchingStrategy?,
                flakinessStrategy: FlakinessStrategy?,
                retryStrategy: RetryStrategy?,

                ignoreFailures: Boolean?,
                isCodeCoverageEnabled: Boolean?,
                fallbackToScreenshots: Boolean?,

                testClassRegexes: Collection<Regex>?,
                includedTestAnnotations: Collection<String>?,
                excludedTestAnnotations: Collection<String>?,
                includeSerialRegexes: Collection<Regex>?,
                excludeSerialRegexes: Collection<Regex>?,

                testOutputTimeoutMillis: Int?,

                testPackage: String?,
                autoGrantPermission: Boolean?,
                vendorConfiguration: VendorConfiguration) :

            this(baseOutputDir = baseOutputDir,
                    outputDir = outputDir,
                    applicationOutput = applicationOutput,
                    testApplicationOutput = testApplicationOutput,
                    poolingStrategy = poolingStrategy ?: OmniPoolingStrategy(),
                    shardingStrategy = shardingStrategy ?: ParallelShardingStrategy(),
                    sortingStrategy = sortingStrategy ?: NoSortingStrategy(),
                    batchingStrategy = batchingStrategy ?: IsolateBatchingStrategy(),
                    flakinessStrategy = flakinessStrategy ?: IgnoreFlakinessStrategy(),
                    retryStrategy = retryStrategy ?: NoRetryStrategy(),
                    ignoreFailures = ignoreFailures ?: false,
                    isCodeCoverageEnabled = isCodeCoverageEnabled ?: false,
                    fallbackToScreenshots = fallbackToScreenshots ?: false,
                    testClassRegexes = testClassRegexes ?: listOf(Regex("^((?!Abstract).)*Test$")),
                    includedTestAnnotations = includedTestAnnotations ?: emptyList(),
                    excludedTestAnnotations = excludedTestAnnotations ?: emptyList(),
                    includeSerialRegexes = includeSerialRegexes ?: emptyList(),
                    excludeSerialRegexes = excludeSerialRegexes ?: emptyList(),
                    testOutputTimeoutMillis = testOutputTimeoutMillis ?: 60_000,
                    testPackage = testPackage ?: "",
                    autoGrantPermission = autoGrantPermission ?: false,
                    vendorConfiguration = vendorConfiguration
            )
}