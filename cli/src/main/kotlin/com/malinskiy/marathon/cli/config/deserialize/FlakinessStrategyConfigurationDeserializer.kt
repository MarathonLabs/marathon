package com.malinskiy.marathon.cli.config.deserialize

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.strategy.FlakinessStrategyConfiguration

class FlakinessStrategyConfigurationDeserializer : StdDeserializer<FlakinessStrategyConfiguration>(FlakinessStrategyConfiguration::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): FlakinessStrategyConfiguration {
        val codec = p?.codec as ObjectMapper
        val node: JsonNode = codec.readTree(p) ?: throw ConfigurationException("Missing flakiness strategy")
        val type = node.get("type").asText()

        return when (type) {
            "ignore" -> FlakinessStrategyConfiguration.IgnoreFlakinessStrategyConfiguration
            "probability" -> {
                (node as ObjectNode).remove("type")
                return codec.treeToValue<FlakinessStrategyConfiguration.ProbabilityBasedFlakinessStrategyConfiguration>(node)
                    ?: throw ConfigurationException("Missing flakiness strategy")
            }
            else -> throw ConfigurationException("Unrecognized flakiness strategy $type")
        }
    }
}
