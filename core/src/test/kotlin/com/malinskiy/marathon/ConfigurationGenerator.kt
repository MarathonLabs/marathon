package com.malinskiy.marathon

import com.malinskiy.marathon.execution.AnalyticsConfiguration
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.strategy.BatchingStrategy
import com.malinskiy.marathon.execution.strategy.FlakinessStrategy
import com.malinskiy.marathon.execution.strategy.PoolingStrategy
import com.malinskiy.marathon.execution.strategy.RetryStrategy
import com.malinskiy.marathon.execution.strategy.ShardingStrategy
import com.malinskiy.marathon.execution.strategy.SortingStrategy
import com.malinskiy.marathon.vendor.VendorConfiguration
import java.io.File

class ConfigurationGenerator {
    fun create(name: String = "",
               baseOutputDir: File = File(""),
               outputDir: File = File(""),
               applicationOutput: File = File(""),
               testApplicationOutput: File = File(""),
               analyticsConfiguration: AnalyticsConfiguration = AnalyticsConfiguration.DisabledAnalytics,
               poolingStrategy: PoolingStrategy? = null,
               shardingStrategy: ShardingStrategy? = null,
               sortingStrategy: SortingStrategy? = null,
               batchingStrategy: BatchingStrategy? = null,
               flakinessStrategy: FlakinessStrategy? = null,
               retryStrategy: RetryStrategy? = null,
               ignoreFailures: Boolean? = null,
               isCodeCoverageEnabled: Boolean? = null,
               fallbackToScreenshots: Boolean? = null,
               testClassRegexes: Collection<Regex>? = null,
               includedTestAnnotations: Collection<String>? = null,
               excludedTestAnnotations: Collection<String>? = null,
               includeSerialRegexes: Collection<Regex>? = null,
               excludeSerialRegexes: Collection<Regex>? = null,
               testOutputTimeoutMillis: Int? = null,
               debug: Boolean? = null,
               testPackage: String? = null,
               autoGrantPermission: Boolean? = null,
               vendorConfiguration: VendorConfiguration = object : VendorConfiguration {}): Configuration = Configuration(
            name = name,
            baseOutputDir = baseOutputDir,
            outputDir = outputDir,
            applicationOutput = applicationOutput,
            testApplicationOutput = testApplicationOutput,
            analyticsConfiguration = analyticsConfiguration,
            poolingStrategy = poolingStrategy,
            shardingStrategy = shardingStrategy,
            sortingStrategy = sortingStrategy,
            batchingStrategy = batchingStrategy,
            flakinessStrategy = flakinessStrategy,
            retryStrategy = retryStrategy,
            ignoreFailures = ignoreFailures,
            isCodeCoverageEnabled = isCodeCoverageEnabled,
            fallbackToScreenshots = fallbackToScreenshots,
            testClassRegexes = testClassRegexes,
            includedTestAnnotations = includedTestAnnotations,
            excludedTestAnnotations = excludedTestAnnotations,
            includeSerialRegexes = includeSerialRegexes,
            excludeSerialRegexes = excludeSerialRegexes,
            testOutputTimeoutMillis = testOutputTimeoutMillis,
            debug = debug,
            testPackage = testPackage,
            autoGrantPermission = autoGrantPermission,
            vendorConfiguration = vendorConfiguration)
}
