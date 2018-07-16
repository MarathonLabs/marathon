package com.malinskiy.marathon.cli.config.deserialize

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.malinskiy.marathon.cli.config.ConfigurationException
import com.malinskiy.marathon.execution.strategy.ShardingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sharding.CountShardingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sharding.ParallelShardingStrategy

class ShardingStrategyDeserializer : StdDeserializer<ShardingStrategy>(ShardingStrategy::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): ShardingStrategy {
        val codec = p?.codec as ObjectMapper
        val node: JsonNode = codec.readTree(p) ?: throw ConfigurationException("Missing sharding strategy")
        val type = node.get("type").asText()

        return when (type) {
            "count" -> {
                (node as ObjectNode).remove("type")
                return codec.treeToValue<CountShardingStrategy>(node)
            }
            "parallel" -> ParallelShardingStrategy()
            else -> throw ConfigurationException("Unrecognized sharding strategy ${type}")
        }
    }
}
