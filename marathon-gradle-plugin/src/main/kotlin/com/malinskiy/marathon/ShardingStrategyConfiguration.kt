package com.malinskiy.marathon

import com.malinskiy.marathon.execution.strategy.CountShardingStrategy
import com.malinskiy.marathon.execution.strategy.ParallelShardingStrategy
import com.malinskiy.marathon.execution.strategy.ShardingStrategy
import groovy.lang.Closure

class ShardingStrategyConfiguration {
    var countSharding: CountShardingStrategyConfiguration? = null

    fun countSharding(closure: Closure<*>) {
        countSharding = CountShardingStrategyConfiguration()
        closure.delegate = countSharding
        closure.call()
    }

    fun countSharding(block: CountShardingStrategyConfiguration.() -> Unit) {
        countSharding = CountShardingStrategyConfiguration().also(block)
    }
}

class CountShardingStrategyConfiguration {
    var count = 1
}

fun ShardingStrategyConfiguration.toStrategy(): ShardingStrategy = countSharding?.let {
    CountShardingStrategy(it.count)
} ?: ParallelShardingStrategy()
