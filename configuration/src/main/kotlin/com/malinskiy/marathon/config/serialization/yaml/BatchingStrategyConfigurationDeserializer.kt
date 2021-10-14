package com.malinskiy.marathon.config.serialization.yaml

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.strategy.BatchingStrategyConfiguration

class BatchingStrategyConfigurationDeserializer : StdDeserializer<BatchingStrategyConfiguration>(BatchingStrategyConfiguration::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): BatchingStrategyConfiguration {
        val codec = p?.codec as ObjectMapper
        val node: JsonNode = codec.readTree(p) ?: throw ConfigurationException("Missing batching strategy")
        val type = node.get("type").asText()

        return when (type) {
            "isolate" -> BatchingStrategyConfiguration.IsolateBatchingStrategyConfiguration
            "fixed-size" -> {
                (node as ObjectNode).remove("type")
                return codec.treeToValue<BatchingStrategyConfiguration.FixedSizeBatchingStrategyConfiguration>(node) ?: throw ConfigurationException(
                    "Missing batching strategy"
                )
            }
            else -> throw ConfigurationException("Unrecognized batching strategy $type")
        }
    }
}
