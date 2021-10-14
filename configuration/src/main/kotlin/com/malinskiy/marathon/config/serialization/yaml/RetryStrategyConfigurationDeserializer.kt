package com.malinskiy.marathon.config.serialization.yaml

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.strategy.RetryStrategyConfiguration

class RetryStrategyConfigurationDeserializer : StdDeserializer<RetryStrategyConfiguration>(RetryStrategyConfiguration::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): RetryStrategyConfiguration {
        val codec = p?.codec as ObjectMapper
        val node: JsonNode = codec.readTree(p) ?: throw ConfigurationException("Missing retry strategy")
        val type = node.get("type").asText()

        return when (type) {
            "no-retry" -> RetryStrategyConfiguration.NoRetryStrategyConfiguration
            "fixed-quota" -> {
                (node as ObjectNode).remove("type")
                return codec.treeToValue<RetryStrategyConfiguration.FixedQuotaRetryStrategyConfiguration>(node) ?: throw ConfigurationException("Missing retry strategy")
            }
            else -> throw ConfigurationException("Unrecognized retry strategy $type")
        }
    }
}
