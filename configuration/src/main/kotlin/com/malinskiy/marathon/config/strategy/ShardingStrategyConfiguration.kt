package com.malinskiy.marathon.config.strategy

import com.fasterxml.jackson.annotation.JsonProperty

sealed class ShardingStrategyConfiguration {
    data class CountShardingStrategyConfiguration(@JsonProperty("count") val count: Int) : ShardingStrategyConfiguration()
    object ParallelShardingStrategyConfiguration : ShardingStrategyConfiguration()
}
