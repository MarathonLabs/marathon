package com.malinskiy.marathon.cli.schema.strategies

import java.io.Serializable

sealed class ShardingStrategy : Serializable {
    object Disabled : ShardingStrategy()
    data class Count(val count: Int) : ShardingStrategy()
}
