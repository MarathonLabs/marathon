package com.malinskiy.marathon

import com.malinskiy.marathon.execution.AnalyticsConfiguration
import groovy.lang.Closure
import org.gradle.api.Project

open class MarathonExtension(project: Project) {
    var name: String = "Marathon"

    var analyticsConfiguration: AnalyticsConfiguration? = null

    var poolingStrategy: PoolingStrategyConfiguration? = null
    var shardingStrategy: ShardingStrategyConfiguration? = null
    var sortingStrategy: SortingStrategyConfiguration? = null
    var batchingStrategy: BatchingStrategyConfiguration? = null
    var flakinessStrategy: FlakinessStrategyConfiguration? = null
    var retryStrategy: RetryStrategyConfiguration? = null

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

    fun retryStrategy(bachStrategyClosure: Closure<*>) {
        retryStrategy = RetryStrategyConfiguration()
        bachStrategyClosure.delegate = retryStrategy
        bachStrategyClosure.call()
    }


}
