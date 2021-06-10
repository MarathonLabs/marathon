package com.malinskiy.marathon.cli.config.deserialize

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.malinskiy.marathon.exceptions.ConfigurationException
import com.malinskiy.marathon.execution.strategy.BatchingStrategy
import com.malinskiy.marathon.execution.strategy.impl.batching.FixedSizeBatchingStrategy
import com.malinskiy.marathon.execution.strategy.impl.batching.IsolateBatchingStrategy

class BatchingStrategyDeserializer : StdDeserializer<BatchingStrategy>(BatchingStrategy::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): BatchingStrategy {
        val codec = p?.codec as ObjectMapper
        val node: JsonNode = codec.readTree(p) ?: throw ConfigurationException("Missing batching strategy")
        val type = node.get("type").asText()

        return when (type) {
            "isolate" -> IsolateBatchingStrategy()
            "fixed-size" -> {
                (node as ObjectNode).remove("type")
                return codec.treeToValue<FixedSizeBatchingStrategy>(node) ?: throw ConfigurationException("Missing batching strategy")
            }
            else -> throw ConfigurationException("Unrecognized batching strategy $type")
        }
    }
}
