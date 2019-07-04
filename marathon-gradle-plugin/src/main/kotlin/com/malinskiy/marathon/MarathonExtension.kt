package com.malinskiy.marathon

import com.malinskiy.marathon.analytics.tracker.Tracker
import com.malinskiy.marathon.device.DeviceFeature
import groovy.lang.Closure
import org.gradle.api.Project

open class MarathonExtension(project: Project) {
    var name: String = "Marathon"

    var customAnalyticsTracker: Tracker? = null
    var analyticsConfiguration: AnalyticsConfig? = null

    var poolingStrategy: PoolingStrategyConfiguration? = null
    var shardingStrategy: ShardingStrategyConfiguration? = null
    var sortingStrategy: SortingStrategyConfiguration? = null
    var batchingStrategy: BatchingStrategyConfiguration? = null
    var flakinessStrategy: FlakinessStrategyConfiguration? = null
    var retryStrategy: RetryStrategyConfiguration? = null
    var filteringConfiguration: FilteringPluginConfiguration? = null

    var baseOutputDir: String? = null

    var ignoreFailures: Boolean? = null
    var isCodeCoverageEnabled: Boolean? = null
    var fallbackToScreenshots: Boolean? = null
    var strictMode: Boolean? = null

    var testClassRegexes: Collection<String>? = null
    var includeSerialRegexes: Collection<String>? = null
    var excludeSerialRegexes: Collection<String>? = null

    var testBatchTimeoutMillis: Long? = null
    var testOutputTimeoutMillis: Long? = null
    var debug: Boolean? = null

    var applicationPmClear: Boolean? = null
    var testApplicationPmClear: Boolean? = null
    var adbInitTimeout: Int? = null
    var installOptions: String? = null

    var preferableRecorderType: DeviceFeature? = null

    var analyticsTracking: Boolean = false

    //Android specific for now
    var autoGrantPermission: Boolean? = null
    var instrumentationArgs: MutableMap<String, String> = mutableMapOf()

    //Kotlin way
    fun analytics(block: AnalyticsConfig.() -> Unit) {
        analyticsConfiguration = AnalyticsConfig().also(block)
    }

    fun batchingStrategy(block: BatchingStrategyConfiguration.() -> Unit) {
        batchingStrategy = BatchingStrategyConfiguration().also(block)
    }

    fun flakinessStrategy(block: FlakinessStrategyConfiguration.() -> Unit) {
        flakinessStrategy = FlakinessStrategyConfiguration().also(block)
    }

    fun poolingStrategy(block: PoolingStrategyConfiguration.() -> Unit) {
        poolingStrategy = PoolingStrategyConfiguration().also(block)
    }

    fun retryStrategy(block: RetryStrategyConfiguration.() -> Unit) {
        retryStrategy = RetryStrategyConfiguration().also(block)
    }

    fun shardingStrategy(block: ShardingStrategyConfiguration.() -> Unit) {
        shardingStrategy = ShardingStrategyConfiguration().also(block)
    }

    fun sortingStrategy(block: SortingStrategyConfiguration.() -> Unit) {
        sortingStrategy = SortingStrategyConfiguration().also(block)
    }

    fun filteringConfiguration(block: FilteringPluginConfiguration.() -> Unit) {
        filteringConfiguration = FilteringPluginConfiguration().also(block)
    }

    fun instrumentationArgs(block: MutableMap<String, String>.() -> Unit) {
        instrumentationArgs = mutableMapOf<String, String>().also(block)
    }

    //Groovy way
    fun analytics(closure: Closure<*>) {
        analyticsConfiguration = AnalyticsConfig()
        closure.delegate = analyticsConfiguration
        closure.call()
    }

    fun batchingStrategy(closure: Closure<*>) {
        batchingStrategy = BatchingStrategyConfiguration()
        closure.delegate = batchingStrategy
        closure.call()
    }

    fun flakinessStrategy(closure: Closure<*>) {
        flakinessStrategy = FlakinessStrategyConfiguration()
        closure.delegate = flakinessStrategy
        closure.call()
    }

    fun poolingStrategy(closure: Closure<*>) {
        poolingStrategy = PoolingStrategyConfiguration()
        closure.delegate = poolingStrategy
        closure.call()
    }

    fun retryStrategy(closure: Closure<*>) {
        retryStrategy = RetryStrategyConfiguration()
        closure.delegate = retryStrategy
        closure.call()
    }

    fun shardingStrategy(closure: Closure<*>) {
        shardingStrategy = ShardingStrategyConfiguration()
        closure.delegate = shardingStrategy
        closure.call()
    }

    fun sortingStrategy(closure: Closure<*>) {
        sortingStrategy = SortingStrategyConfiguration()
        closure.delegate = sortingStrategy
        closure.call()
    }

    fun filteringConfiguration(closure: Closure<*>) {
        filteringConfiguration = FilteringPluginConfiguration()
        closure.delegate = filteringConfiguration
        closure.call()
    }

    fun instrumentationArgs(closure: Closure<*>) {
        instrumentationArgs = mutableMapOf()
        closure.delegate = instrumentationArgs
        closure.call()
    }
}
