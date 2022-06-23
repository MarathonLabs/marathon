package com.malinskiy.marathon.gradle

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.util.internal.ConfigureUtil
import java.time.Instant

class BatchingStrategyConfiguration {
    var fixedSize: FixedSizeBatchingStrategyConfiguration? = null

    fun fixedSize(action: Action<FixedSizeBatchingStrategyConfiguration>) {
        fixedSize = FixedSizeBatchingStrategyConfiguration().also { action.execute(it) }
    }

    fun fixedSize(closure: Closure<FixedSizeBatchingStrategyConfiguration>) = fixedSize(ConfigureUtil.configureUsing(closure))
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
