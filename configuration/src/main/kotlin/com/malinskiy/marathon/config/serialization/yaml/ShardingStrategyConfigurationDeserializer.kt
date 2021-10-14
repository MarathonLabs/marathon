package com.malinskiy.marathon.config.serialization.yaml

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.strategy.ShardingStrategyConfiguration

class ShardingStrategyConfigurationDeserializer :
    StdDeserializer<ShardingStrategyConfiguration>(ShardingStrategyConfiguration::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): ShardingStrategyConfiguration {
        val codec = p?.codec as ObjectMapper
        val node: JsonNode = codec.readTree(p) ?: throw ConfigurationException("Missing sharding strategy")
        val type = node.get("type").asText()

        return when (type) {
            "count" -> {
                (node as ObjectNode).remove("type")
                return codec.treeToValue<ShardingStrategyConfiguration.CountShardingStrategyConfiguration>(node)
                    ?: throw ConfigurationException("Missing sharding strategy")
            }
            "parallel" -> ShardingStrategyConfiguration.ParallelShardingStrategyConfiguration
            else -> throw ConfigurationException("Unrecognized sharding strategy $type")
        }
    }
}
