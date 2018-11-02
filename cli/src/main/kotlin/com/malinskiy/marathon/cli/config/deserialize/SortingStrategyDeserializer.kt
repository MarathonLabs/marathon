package com.malinskiy.marathon.cli.config.deserialize

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.malinskiy.marathon.exceptions.ConfigurationException
import com.malinskiy.marathon.execution.strategy.SortingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sorting.ExecutionTimeSortingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sorting.NoSortingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sorting.SuccessRateSortingStrategy

class SortingStrategyDeserializer : StdDeserializer<SortingStrategy>(SortingStrategy::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): SortingStrategy {
        val codec = p?.codec as ObjectMapper
        val node: JsonNode = codec.readTree(p) ?: throw ConfigurationException("Missing sorting strategy")
        val type = node.get("type").asText()

        return when (type) {
            "no-sorting" -> NoSortingStrategy()
            "success-rate" -> {
                (node as ObjectNode).remove("type")
                codec.treeToValue<SuccessRateSortingStrategy>(node)
            }
            "execution-time" -> {
                (node as ObjectNode).remove("type")
                codec.treeToValue<ExecutionTimeSortingStrategy>(node)
            }
            else -> throw ConfigurationException("Unrecognized sorting strategy $type")
        }
    }
}
