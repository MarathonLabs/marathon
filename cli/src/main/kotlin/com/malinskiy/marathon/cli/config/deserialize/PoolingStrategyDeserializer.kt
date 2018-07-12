package com.malinskiy.marathon.cli.config.deserialize

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ArrayNode
import com.malinskiy.marathon.execution.strategy.PoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.OmniPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized.*

class PoolingStrategyDeserializer: StdDeserializer<PoolingStrategy>(PoolingStrategy::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): PoolingStrategy {
        val node: JsonNode? = p?.codec?.readTree(p)

        val list = mutableListOf<PoolingStrategy>()

        if(node == null) throw RuntimeException("Missing pooling strategy")
        if(!node.isArray) throw RuntimeException("Pooling strategy should be an array")


        val arrayNode = node as ArrayNode
        arrayNode.forEach {
            val type = it.get("type").asText()
            list.add(
                    when (type) {
                        "omni" -> OmniPoolingStrategy()
                        "device-model" -> ModelPoolingStrategy()
                        "os-version" -> OperatingSystemVersionPoolingStrategy()
                        "manufacturer" -> ManufacturerPoolingStrategy()
                        "abi" -> AbiPoolingStrategy()
                        else -> throw RuntimeException("Unrecognized pooling strategy ${type}")
                    }
            )
        }

        return ComboPoolingStrategy(list)
    }
}