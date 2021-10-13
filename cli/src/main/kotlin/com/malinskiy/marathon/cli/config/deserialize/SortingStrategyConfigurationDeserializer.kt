package com.malinskiy.marathon.cli.config.deserialize

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.strategy.SortingStrategyConfiguration

class SortingStrategyConfigurationDeserializer : StdDeserializer<SortingStrategyConfiguration>(SortingStrategyConfiguration::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): SortingStrategyConfiguration {
        val codec = p?.codec as ObjectMapper
        val node: JsonNode = codec.readTree(p) ?: throw ConfigurationException("Missing sorting strategy")
        val type = node.get("type").asText()

        return when (type) {
            "no-sorting" -> SortingStrategyConfiguration.NoSortingStrategyConfiguration
            "success-rate" -> {
                (node as ObjectNode).remove("type")
                codec.treeToValue(node, SortingStrategyConfiguration.SuccessRateSortingStrategyConfiguration::class.java)
            }
            "execution-time" -> {
                (node as ObjectNode).remove("type")
                codec.treeToValue(node, SortingStrategyConfiguration.ExecutionTimeSortingStrategyConfiguration::class.java)
            }
            else -> throw ConfigurationException("Unrecognized sorting strategy $type")
        }
    }
}
