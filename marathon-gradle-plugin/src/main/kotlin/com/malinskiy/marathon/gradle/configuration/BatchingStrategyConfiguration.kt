package com.malinskiy.marathon.gradle

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.util.internal.ConfigureUtil
import java.time.Instant

class BatchingStrategyConfiguration {
    var fixedSize: FixedSizeBatchingStrategyConfiguration? = null
    var className: ClassNameBatchingStrategyConfiguration? = null

    fun fixedSize(action: Action<FixedSizeBatchingStrategyConfiguration>) {
        fixedSize = FixedSizeBatchingStrategyConfiguration().also { action.execute(it) }
    }

    fun fixedSize(closure: Closure<FixedSizeBatchingStrategyConfiguration>) = fixedSize(ConfigureUtil.configureUsing(closure))

    fun className(action: Action<ClassNameBatchingStrategyConfiguration>) {
        className = ClassNameBatchingStrategyConfiguration
    }

    fun className(closure: Closure<ClassNameBatchingStrategyConfiguration>) = className(ConfigureUtil.configureUsing(closure))
}

class FixedSizeBatchingStrategyConfiguration {
    var size = 1
    var durationMillis: Long? = null
    var percentile: Double? = null
    var timeLimit: Instant? = null
    var lastMileLength: Int = 0
}

object ClassNameBatchingStrategyConfiguration

fun BatchingStrategyConfiguration.toStrategy(): com.malinskiy.marathon.config.strategy.BatchingStrategyConfiguration {
    var batchingConfigurationCount = 0
    if (fixedSize != null) batchingConfigurationCount++
    if (className != null) batchingConfigurationCount++
    if (batchingConfigurationCount > 1) throw RuntimeException("Only one batching strategy can be selected")

    return fixedSize?.let {
        com.malinskiy.marathon.config.strategy.BatchingStrategyConfiguration.FixedSizeBatchingStrategyConfiguration(
            it.size,
            it.durationMillis,
            it.percentile,
            it.timeLimit,
            it.lastMileLength
        )
    }
        ?: className?.let { com.malinskiy.marathon.config.strategy.BatchingStrategyConfiguration.ClassNameBatchingStrategyConfiguration }
        ?: com.malinskiy.marathon.config.strategy.BatchingStrategyConfiguration.IsolateBatchingStrategyConfiguration
}
