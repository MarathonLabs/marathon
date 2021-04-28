package com.malinskiy.marathon

import com.malinskiy.marathon.execution.strategy.ShardingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sharding.CountShardingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sharding.ParallelShardingStrategy
import groovy.lang.Closure
import org.gradle.api.Action
import java.io.Serializable

class ShardingStrategyConfiguration : Serializable {
    var countSharding: CountShardingStrategyConfiguration? = null

    fun countSharding(closure: Closure<*>) {
        countSharding = CountShardingStrategyConfiguration()
        closure.delegate = countSharding
        closure.call()
    }

    fun countSharding(action: Action<CountShardingStrategyConfiguration>) {
        countSharding = CountShardingStrategyConfiguration().also(action::execute)
    }
}

class CountShardingStrategyConfiguration : Serializable{
    var count = 1
}

fun ShardingStrategyConfiguration.toStrategy(): ShardingStrategy = countSharding?.let {
    CountShardingStrategy(it.count)
} ?: ParallelShardingStrategy()
