package com.malinskiy.marathon.gradle

import org.gradle.api.Action

class ShardingStrategyConfiguration {
    var countSharding: CountShardingStrategyConfiguration? = null

    fun countSharding(action: Action<CountShardingStrategyConfiguration>) {
        countSharding = CountShardingStrategyConfiguration().also { action.execute(it) }
    }
}

class CountShardingStrategyConfiguration {
    var count = 1
}

fun ShardingStrategyConfiguration.toStrategy(): com.malinskiy.marathon.config.strategy.ShardingStrategyConfiguration = countSharding?.let {
    com.malinskiy.marathon.config.strategy.ShardingStrategyConfiguration.CountShardingStrategyConfiguration(it.count)
} ?: com.malinskiy.marathon.config.strategy.ShardingStrategyConfiguration.ParallelShardingStrategyConfiguration
