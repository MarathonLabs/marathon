package com.malinskiy.marathon.config

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.malinskiy.marathon.config.strategy.BatchingStrategyConfiguration
import com.malinskiy.marathon.config.strategy.ExecutionStrategyConfiguration
import com.malinskiy.marathon.config.strategy.FlakinessStrategyConfiguration
import com.malinskiy.marathon.config.strategy.PoolingStrategyConfiguration
import com.malinskiy.marathon.config.strategy.RetryStrategyConfiguration
import com.malinskiy.marathon.config.strategy.ShardingStrategyConfiguration
import com.malinskiy.marathon.config.strategy.SortingStrategyConfiguration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import java.io.File

private const val DEFAULT_BATCH_EXECUTION_TIMEOUT_MILLIS: Long = 1800_000 //30 min
private const val DEFAULT_OUTPUT_TIMEOUT_MILLIS: Long = 300_000 //5 min
private const val DEFAULT_DEVICE_INITIALIZATION_TIMEOUT_MILLIS = 180_000L

@JsonDeserialize(builder = Configuration.Builder::class)
data class Configuration private constructor(
    val name: String,
    val outputDir: File,

    val outputConfiguration: OutputConfiguration,

    val analyticsConfiguration: AnalyticsConfiguration,
    val poolingStrategy: PoolingStrategyConfiguration,
    val shardingStrategy: ShardingStrategyConfiguration,
    val sortingStrategy: SortingStrategyConfiguration,
    val batchingStrategy: BatchingStrategyConfiguration,
    val flakinessStrategy: FlakinessStrategyConfiguration,
    val retryStrategy: RetryStrategyConfiguration,
    val filteringConfiguration: FilteringConfiguration,

    val ignoreFailures: Boolean,
    val executionStrategy: ExecutionStrategyConfiguration,
    val isCodeCoverageEnabled: Boolean,
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
    val deviceInitializationTimeoutMillis: Long,
) {
    fun toMap() =
        mapOf<String, String>(
            "name" to name,
            "outputDir" to outputDir.absolutePath,
            "outputConfiguration" to outputConfiguration.toString(),
            "analyticsConfiguration" to analyticsConfiguration.toString(),
            "pooling" to poolingStrategy.toString(),
            "sharding" to shardingStrategy.toString(),
            "sorting" to sortingStrategy.toString(),
            "batching" to batchingStrategy.toString(),
            "flakiness" to flakinessStrategy.toString(),
            "retry" to retryStrategy.toString(),
            "filtering" to filteringConfiguration.toString(),
            "ignoreFailures" to ignoreFailures.toString(),
            "isCodeCoverageEnabled" to isCodeCoverageEnabled.toString(),
            "executionStrategy" to executionStrategy.toString(),
            "testClassRegexes" to testClassRegexes.toString(),
            "includeSerialRegexes" to includeSerialRegexes.toString(),
            "excludeSerialRegexes" to excludeSerialRegexes.toString(),
            "testBatchTimeoutMillis" to testBatchTimeoutMillis.toString(),
            "testOutputTimeoutMillis" to testOutputTimeoutMillis.toString(),
            "debug" to debug.toString(),
            "screenRecordingPolicy" to screenRecordingPolicy.toString(),
            "vendorConfiguration" to vendorConfiguration.toString(),
            "deviceInitializationTimeoutMillis" to deviceInitializationTimeoutMillis.toString()
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Configuration

        if (name != other.name) return false
        if (outputDir != other.outputDir) return false
        if (outputConfiguration != other.outputConfiguration) return false
        if (analyticsConfiguration != other.analyticsConfiguration) return false
        if (poolingStrategy != other.poolingStrategy) return false
        if (shardingStrategy != other.shardingStrategy) return false
        if (sortingStrategy != other.sortingStrategy) return false
        if (batchingStrategy != other.batchingStrategy) return false
        if (flakinessStrategy != other.flakinessStrategy) return false
        if (retryStrategy != other.retryStrategy) return false
        if (filteringConfiguration != other.filteringConfiguration) return false
        if (ignoreFailures != other.ignoreFailures) return false
        if (isCodeCoverageEnabled != other.isCodeCoverageEnabled) return false
        if (executionStrategy != other.executionStrategy) return false
        if (uncompletedTestRetryQuota != other.uncompletedTestRetryQuota) return false
        //For testing we need to compare configuration instances. Unfortunately Regex equality is broken so need to map it to String
        if (testClassRegexes.map { it.pattern } != other.testClassRegexes.map { it.pattern }) return false
        if (includeSerialRegexes.map { it.pattern } != other.includeSerialRegexes.map { it.pattern }) return false
        if (excludeSerialRegexes.map { it.pattern } != other.excludeSerialRegexes.map { it.pattern }) return false
        if (testBatchTimeoutMillis != other.testBatchTimeoutMillis) return false
        if (testOutputTimeoutMillis != other.testOutputTimeoutMillis) return false
        if (debug != other.debug) return false
        if (screenRecordingPolicy != other.screenRecordingPolicy) return false
        if (vendorConfiguration != other.vendorConfiguration) return false
        if (analyticsTracking != other.analyticsTracking) return false
        if (deviceInitializationTimeoutMillis != other.deviceInitializationTimeoutMillis) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + outputDir.hashCode()
        result = 31 * result + outputConfiguration.hashCode()
        result = 31 * result + analyticsConfiguration.hashCode()
        result = 31 * result + poolingStrategy.hashCode()
        result = 31 * result + shardingStrategy.hashCode()
        result = 31 * result + sortingStrategy.hashCode()
        result = 31 * result + batchingStrategy.hashCode()
        result = 31 * result + flakinessStrategy.hashCode()
        result = 31 * result + retryStrategy.hashCode()
        result = 31 * result + filteringConfiguration.hashCode()
        result = 31 * result + ignoreFailures.hashCode()
        result = 31 * result + isCodeCoverageEnabled.hashCode()
        result = 31 * result + executionStrategy.hashCode()
        result = 31 * result + uncompletedTestRetryQuota
        result = 31 * result + testClassRegexes.hashCode()
        result = 31 * result + includeSerialRegexes.hashCode()
        result = 31 * result + excludeSerialRegexes.hashCode()
        result = 31 * result + testBatchTimeoutMillis.hashCode()
        result = 31 * result + testOutputTimeoutMillis.hashCode()
        result = 31 * result + debug.hashCode()
        result = 31 * result + screenRecordingPolicy.hashCode()
        result = 31 * result + vendorConfiguration.hashCode()
        result = 31 * result + analyticsTracking.hashCode()
        result = 31 * result + deviceInitializationTimeoutMillis.hashCode()
        return result
    }
     data class Builder(
         val name: String,
         val outputDir: File,
         var analyticsConfiguration: AnalyticsConfiguration = AnalyticsConfiguration.DisabledAnalytics,
         var poolingStrategy: PoolingStrategyConfiguration = PoolingStrategyConfiguration.OmniPoolingStrategyConfiguration,
         var shardingStrategy: ShardingStrategyConfiguration = ShardingStrategyConfiguration.ParallelShardingStrategyConfiguration,
         var sortingStrategy: SortingStrategyConfiguration = SortingStrategyConfiguration.NoSortingStrategyConfiguration,
         var batchingStrategy: BatchingStrategyConfiguration = BatchingStrategyConfiguration.IsolateBatchingStrategyConfiguration,
         var flakinessStrategy: FlakinessStrategyConfiguration = FlakinessStrategyConfiguration.IgnoreFlakinessStrategyConfiguration,
         var retryStrategy: RetryStrategyConfiguration = RetryStrategyConfiguration.NoRetryStrategyConfiguration,
         var filteringConfiguration: FilteringConfiguration = FilteringConfiguration(emptyList(), emptyList()),

         var ignoreFailures: Boolean = false,
         var isCodeCoverageEnabled: Boolean = false,
         var executionStrategy: ExecutionStrategyConfiguration = ExecutionStrategyConfiguration(),
         var uncompletedTestRetryQuota: Int = Integer.MAX_VALUE,

         var testClassRegexes: Collection<Regex> = listOf(Regex("^((?!Abstract).)*Test[s]*$")),
         var includeSerialRegexes: Collection<Regex> = emptyList(),
         var excludeSerialRegexes: Collection<Regex> = emptyList(),

         var testBatchTimeoutMillis: Long = DEFAULT_BATCH_EXECUTION_TIMEOUT_MILLIS,
         var testOutputTimeoutMillis: Long = DEFAULT_OUTPUT_TIMEOUT_MILLIS,
         var debug: Boolean = true,

         var screenRecordingPolicy: ScreenRecordingPolicy = ScreenRecordingPolicy.ON_FAILURE,

         var analyticsTracking: Boolean = false,
         var deviceInitializationTimeoutMillis: Long = DEFAULT_DEVICE_INITIALIZATION_TIMEOUT_MILLIS,

         var outputConfiguration: OutputConfiguration = OutputConfiguration(),
         var vendorConfiguration: VendorConfiguration = VendorConfiguration.EmptyVendorConfiguration(),
    ) {
        fun build(): Configuration {
            return Configuration(
                name = name,
                outputDir = outputDir,
                outputConfiguration = outputConfiguration,
                analyticsConfiguration = analyticsConfiguration,
                poolingStrategy = poolingStrategy,
                shardingStrategy = shardingStrategy,
                sortingStrategy = sortingStrategy,
                batchingStrategy = batchingStrategy,
                flakinessStrategy = flakinessStrategy,
                retryStrategy = retryStrategy,
                filteringConfiguration = filteringConfiguration,
                ignoreFailures = ignoreFailures,
                isCodeCoverageEnabled = isCodeCoverageEnabled,
                executionStrategy = executionStrategy,
                uncompletedTestRetryQuota = uncompletedTestRetryQuota,
                testClassRegexes = testClassRegexes,
                includeSerialRegexes = includeSerialRegexes,
                excludeSerialRegexes = excludeSerialRegexes,
                testBatchTimeoutMillis = testBatchTimeoutMillis,
                testOutputTimeoutMillis = testOutputTimeoutMillis,
                debug = debug,
                screenRecordingPolicy = screenRecordingPolicy,
                vendorConfiguration = vendorConfiguration,
                analyticsTracking = analyticsTracking,
                deviceInitializationTimeoutMillis = deviceInitializationTimeoutMillis
            )
        }
    }
}
