package com.malinskiy.marathon

import com.malinskiy.marathon.execution.strategy.*

data class MarathonPluginConfiguration @JvmOverloads constructor(
        val name: String,

        var poolingStrategy: PoolingStrategy? = null,
        var shardingStrategy: ShardingStrategy? = null,
        var sortingStrategy: SortingStrategy? = null,
        var batchingStrategy: BatchingStrategy? = null,
        var flakinessStrategy: FlakinessStrategy? = null,
        var retryStrategy: RetryStrategy? = null,

        var baseOutputDir: String? = null,

        var ignoreFailures: Boolean? = null,
        var isCodeCoverageEnabled: Boolean? = null,
        var fallbackToScreenshots: Boolean? = null,

        var testClassRegexes: Collection<String>? = null,
        var includedTestAnnotations: Collection<String>? = null,
        var excludedTestAnnotations: Collection<String>? = null,
        var includeSerialRegexes: Collection<String>? = null,
        var excludeSerialRegexes: Collection<String>? = null,

        var testOutputTimeoutMillis: Int? = null,
        var debug: Boolean? = null,

        //Android specific for now
        var testPackage: String? = null,
        var autoGrantPermission: Boolean? = null
)
