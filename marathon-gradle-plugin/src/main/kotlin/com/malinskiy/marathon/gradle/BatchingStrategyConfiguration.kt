package com.malinskiy.marathon.gradle

import groovy.lang.Closure
import java.time.Instant

class BatchingStrategyConfiguration {
    var fixedSize: FixedSizeBatchingStrategyConfiguration? = null

    fun fixedSize(block: FixedSizeBatchingStrategyConfiguration.() -> Unit) {
        fixedSize = FixedSizeBatchingStrategyConfiguration().also(block)
    }

    fun fixedSize(closure: Closure<*>) {
        fixedSize = FixedSizeBatchingStrategyConfiguration()
        closure.delegate = fixedSize
        closure.call()
    }
}

class FixedSizeBatchingStrategyConfiguration {
    var size = 1
    var durationMillis: Long? = null
    var percentile: Double? = null
    var timeLimit: Instant? = null
    var lastMileLength: Int = 0
}

fun BatchingStrategyConfiguration.toStrategy(): com.malinskiy.marathon.config.strategy.BatchingStrategyConfiguration = fixedSize?.let {
    com.malinskiy.marathon.config.strategy.BatchingStrategyConfiguration.FixedSizeBatchingStrategyConfiguration(
        it.size,
        it.durationMillis,
        it.percentile,
        it.timeLimit,
        it.lastMileLength
    )
} ?: com.malinskiy.marathon.config.strategy.BatchingStrategyConfiguration.IsolateBatchingStrategyConfiguration
