package com.malinskiy.marathon.cli.config.deserialize

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ArrayNode
import com.malinskiy.marathon.exceptions.ConfigurationException
import com.malinskiy.marathon.execution.strategy.PoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.OmniPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized.AbiPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized.ComboPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized.ManufacturerPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized.ModelPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized.OperatingSystemVersionPoolingStrategy

class PoolingStrategyDeserializer: StdDeserializer<PoolingStrategy>(PoolingStrategy::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): PoolingStrategy {
        val node: JsonNode? = p?.codec?.readTree(p)

        val list = mutableListOf<PoolingStrategy>()

        if(node == null) throw ConfigurationException("Missing pooling strategy")
        if(!node.isArray) throw ConfigurationException("Pooling strategy should be an array")


        val arrayNode = node as ArrayNode
        arrayNode.forEach {
            val type = it.get("type").asText()
            list.add(
                    deserializeStrategy(type)
            )
        }

        return ComboPoolingStrategy(list)
    }

    private fun deserializeStrategy(type: String?): PoolingStrategy {
        return when (type) {
            "omni" -> OmniPoolingStrategy()
            "device-model" -> ModelPoolingStrategy()
            "os-version" -> OperatingSystemVersionPoolingStrategy()
            "manufacturer" -> ManufacturerPoolingStrategy()
            "abi" -> AbiPoolingStrategy()
            else -> throw ConfigurationException("Unrecognized pooling strategy $type")
        }
    }
}
