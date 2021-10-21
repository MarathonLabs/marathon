package com.malinskiy.marathon.config.strategy

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ShardingStrategyConfiguration.CountShardingStrategyConfiguration::class, name = "count"),
    JsonSubTypes.Type(value = ShardingStrategyConfiguration.ParallelShardingStrategyConfiguration::class, name = "parallel"),
)
sealed class ShardingStrategyConfiguration {
    data class CountShardingStrategyConfiguration(@JsonProperty("count") val count: Int) : ShardingStrategyConfiguration()
    object ParallelShardingStrategyConfiguration : ShardingStrategyConfiguration()
}
