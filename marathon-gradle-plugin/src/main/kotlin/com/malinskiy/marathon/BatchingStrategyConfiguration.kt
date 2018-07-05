package com.malinskiy.marathon

import com.malinskiy.marathon.execution.strategy.BatchingStrategy
import com.malinskiy.marathon.execution.strategy.impl.batching.FixedSizeBatchingStrategy
import com.malinskiy.marathon.execution.strategy.impl.batching.IsolateBatchingStrategy
import groovy.lang.Closure

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
}

fun BatchingStrategyConfiguration.toStrategy(): BatchingStrategy = fixedSize?.let {
    FixedSizeBatchingStrategy(it.size)
} ?: IsolateBatchingStrategy()