package com.malinskiy.marathon.execution

import com.malinskiy.marathon.analytics.metrics.remote.influx.SuccessRate
import com.malinskiy.marathon.analytics.tracker.remote.influx.InfluxDbProvider
import com.malinskiy.marathon.execution.strategy.BatchingStrategy
import com.malinskiy.marathon.execution.strategy.FlakinessStrategy
import com.malinskiy.marathon.execution.strategy.PoolingStrategy
import com.malinskiy.marathon.execution.strategy.RetryStrategy
import com.malinskiy.marathon.execution.strategy.ShardingStrategy
import com.malinskiy.marathon.execution.strategy.SortingStrategy
import com.malinskiy.marathon.execution.strategy.impl.batching.FixedSizeBatchingStrategy
import com.malinskiy.marathon.execution.strategy.impl.batching.IsolateBatchingStrategy
import com.malinskiy.marathon.execution.strategy.impl.flakiness.IgnoreFlakinessStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.OmniPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.retry.NoRetryStrategy
import com.malinskiy.marathon.execution.strategy.impl.sharding.ParallelShardingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sharding.TempShardingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sorting.ExecutionTimeSortingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sorting.NoSortingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sorting.SuccessRateSortingStrategy
import com.malinskiy.marathon.vendor.VendorConfiguration
import java.io.File


data class Configuration constructor(
        val name: String,
        val baseOutputDir: File,
        val outputDir: File,
        val applicationOutput: File,
        val testApplicationOutput: File,

        val analyticsConfiguration: AnalyticsConfiguration,
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
        val debug: Boolean,

        val testPackage: String?,
        val autoGrantPermission: Boolean,
        val vendorConfiguration: VendorConfiguration) {

    companion object {
        private const val DEFAULT_OUTPUT_TIMEOUT = 60_000
    }

    constructor(name: String,
                baseOutputDir: File,
                outputDir: File,
                applicationOutput: File,
                testApplicationOutput: File,

                analyticsConfiguration: AnalyticsConfiguration?,
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
                debug: Boolean?,

                testPackage: String?,
                autoGrantPermission: Boolean?,
                vendorConfiguration: VendorConfiguration) :

            this(name = name,
                    baseOutputDir = baseOutputDir,
                    outputDir = outputDir,
                    applicationOutput = applicationOutput,
                    testApplicationOutput = testApplicationOutput,
//                    analyticsConfiguration = analyticsConfiguration ?: AnalyticsConfiguration.DisabledAnalytics, //TODO: Revert
                    analyticsConfiguration = analyticsConfiguration ?: AnalyticsConfiguration.InfluxDbConfiguration(
                            url = "http://localhost:8086",
                            user = "root",
                            password = "root",
                            dbName = "tests",
                            retentionPolicyConfiguration = AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration.default),
                    poolingStrategy = poolingStrategy ?: OmniPoolingStrategy(),
                    shardingStrategy = shardingStrategy ?: ParallelShardingStrategy(), // TODO: Revert
//                    sortingStrategy = sortingStrategy ?: NoSortingStrategy(),
                    sortingStrategy = sortingStrategy ?: ExecutionTimeSortingStrategy(90.0),
//                    batchingStrategy = batchingStrategy ?: IsolateBatchingStrategy(), //TODO: Revert
                    batchingStrategy = FixedSizeBatchingStrategy(5),
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
                    testOutputTimeoutMillis = testOutputTimeoutMillis ?: DEFAULT_OUTPUT_TIMEOUT,
                    debug = debug ?: true,
                    testPackage = testPackage ?: "",
                    autoGrantPermission = autoGrantPermission ?: false,
                    vendorConfiguration = vendorConfiguration
            )
}
