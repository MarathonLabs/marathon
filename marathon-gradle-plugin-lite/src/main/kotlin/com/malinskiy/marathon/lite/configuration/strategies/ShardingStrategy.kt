package com.malinskiy.marathon.lite.configuration.strategies

import java.io.Serializable

sealed class ShardingStrategy : Serializable {
    object Disabled : ShardingStrategy()
    data class Count(val count: Int) : ShardingStrategy()
}
