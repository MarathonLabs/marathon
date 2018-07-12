package com.malinskiy.marathon.cli.config.deserialize

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.malinskiy.marathon.execution.strategy.FlakinessStrategy
import com.malinskiy.marathon.execution.strategy.impl.flakiness.IgnoreFlakinessStrategy
import com.malinskiy.marathon.execution.strategy.impl.flakiness.ProbabilityBasedFlakinessStrategy

class FlakinessStrategyDeserializer : StdDeserializer<FlakinessStrategy>(FlakinessStrategy::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): FlakinessStrategy {
        val codec = p?.codec as ObjectMapper
        val node: JsonNode = codec.readTree(p) ?: throw RuntimeException("Missing flakiness strategy")
        val type = node.get("type").asText()

        return when (type) {
            "ignore" -> IgnoreFlakinessStrategy()
            "probability" -> {
                (node as ObjectNode).remove("type")
                return codec.treeToValue<ProbabilityBasedFlakinessStrategy>(node)
            }
            else -> throw RuntimeException("Unrecognized flakiness strategy $type")
        }
    }
}