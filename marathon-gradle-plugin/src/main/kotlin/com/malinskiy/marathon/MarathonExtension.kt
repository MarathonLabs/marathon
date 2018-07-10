package com.malinskiy.marathon

import groovy.lang.Closure
import org.gradle.api.Project

open class MarathonExtension(project: Project) {
    var name: String = "Marathon"

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

    var testClassRegexes: Collection<String>? = null
    var includedTestAnnotations: Collection<String>? = null
    var excludedTestAnnotations: Collection<String>? = null
    var includeSerialRegexes: Collection<String>? = null
    var excludeSerialRegexes: Collection<String>? = null

    var testOutputTimeoutMillis: Int? = null
    var debug: Boolean? = null

    //Android specific for now
    var testPackage: String? = null
    var autoGrantPermission: Boolean? = null

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
}
