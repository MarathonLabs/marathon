package com.malinskiy.marathon

import com.malinskiy.marathon.execution.AnalyticsConfiguration
import com.malinskiy.marathon.execution.strategy.BatchingStrategy
import com.malinskiy.marathon.execution.strategy.FlakinessStrategy
import com.malinskiy.marathon.execution.strategy.PoolingStrategy
import com.malinskiy.marathon.execution.strategy.RetryStrategy
import com.malinskiy.marathon.execution.strategy.ShardingStrategy
import com.malinskiy.marathon.execution.strategy.SortingStrategy
import org.gradle.api.Project

open class MarathonExtension(project: Project) {
    var name: String = "Marathon"

    var analyticsConfiguration: AnalyticsConfiguration? = null

    var poolingStrategy: PoolingStrategy? = null
    var shardingStrategy: ShardingStrategy? = null
    var sortingStrategy: SortingStrategy? = null
    var batchingStrategy: BatchingStrategy? = null
    var flakinessStrategy: FlakinessStrategy? = null
    var retryStrategy: RetryStrategy? = null

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
}
