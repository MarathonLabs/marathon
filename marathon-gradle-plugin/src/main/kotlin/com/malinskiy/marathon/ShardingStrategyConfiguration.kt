package com.malinskiy.marathon

import com.malinskiy.marathon.execution.strategy.ShardingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sharding.CountShardingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sharding.ParallelShardingStrategy
import groovy.lang.Closure

class ShardingStrategyConfiguration {
    var countSharding: CountShardingStrategyConfiguration? = null

    fun countSharding(closure: Closure<*>) {
        countSharding = CountShardingStrategyConfiguration()
        closure.delegate = countSharding
        closure.call()
    }
}

class CountShardingStrategyConfiguration {
    var count = 1
}

fun ShardingStrategyConfiguration.toStrategy(): ShardingStrategy = countSharding?.let {
    CountShardingStrategy(it.count)
} ?: ParallelShardingStrategy()
