package com.malinskiy.marathon.cli.config.deserialize

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ArrayNode
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.strategy.PoolingStrategyConfiguration

class PoolingStrategyConfigurationDeserializer : StdDeserializer<PoolingStrategyConfiguration>(PoolingStrategyConfiguration::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): PoolingStrategyConfiguration {
        val node: JsonNode? = p?.codec?.readTree(p)

        val list = mutableListOf<PoolingStrategyConfiguration>()

        if (node == null) throw ConfigurationException("Missing pooling strategy")
        if (!node.isArray) throw ConfigurationException("Pooling strategy should be an array")


        val arrayNode = node as ArrayNode
        arrayNode.forEach {
            val type = it.get("type").asText()
            list.add(
                deserializeStrategy(type)
            )
        }

        return PoolingStrategyConfiguration.ComboPoolingStrategyConfiguration(list)
    }

    private fun deserializeStrategy(type: String?): PoolingStrategyConfiguration {
        return when (type) {
            "omni" -> PoolingStrategyConfiguration.OmniPoolingStrategyConfiguration
            "device-model" -> PoolingStrategyConfiguration.ModelPoolingStrategyConfiguration
            "os-version" -> PoolingStrategyConfiguration.OperatingSystemVersionPoolingStrategyConfiguration
            "manufacturer" -> PoolingStrategyConfiguration.ManufacturerPoolingStrategyConfiguration
            "abi" -> PoolingStrategyConfiguration.AbiPoolingStrategyConfiguration
            else -> throw ConfigurationException("Unrecognized pooling strategy $type")
        }
    }
}
