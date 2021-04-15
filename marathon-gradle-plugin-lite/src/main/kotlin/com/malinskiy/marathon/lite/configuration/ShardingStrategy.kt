package com.malinskiy.marathon.lite.configuration

import java.io.Serializable
import com.malinskiy.marathon.cliconfig.proto.ShardingStrategy as ProtoShardingStrategy

sealed class ShardingStrategy : Serializable {
    object Disabled : ShardingStrategy()
    data class Count(val count: Int) : ShardingStrategy()
}

fun ShardingStrategy.toProto(): ProtoShardingStrategy {
    val builder = ProtoShardingStrategy.newBuilder();
    when (this) {
        ShardingStrategy.Disabled -> builder.disabled = ProtoShardingStrategy.Disabled.getDefaultInstance()
        is ShardingStrategy.Count -> builder.count = ProtoShardingStrategy.Count.newBuilder().setCount(count).build()
    }
    return builder.build();
}
