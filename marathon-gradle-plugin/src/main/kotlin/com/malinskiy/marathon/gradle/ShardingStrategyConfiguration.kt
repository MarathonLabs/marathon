package com.malinskiy.marathon.gradle

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

fun ShardingStrategyConfiguration.toStrategy(): com.malinskiy.marathon.config.strategy.ShardingStrategyConfiguration = countSharding?.let {
    com.malinskiy.marathon.config.strategy.ShardingStrategyConfiguration.CountShardingStrategyConfiguration(it.count)
} ?: com.malinskiy.marathon.config.strategy.ShardingStrategyConfiguration.ParallelShardingStrategyConfiguration
