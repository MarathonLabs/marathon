package com.malinskiy.marathon.cli.schema.strategies

sealed class ShardingStrategy {
    object Disabled : ShardingStrategy()
    data class Count(val count: Int) : ShardingStrategy()
}
