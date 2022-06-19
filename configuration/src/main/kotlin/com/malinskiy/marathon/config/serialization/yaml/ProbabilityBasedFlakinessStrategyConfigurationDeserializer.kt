package com.malinskiy.marathon.config.serialization.yaml

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.serialization.time.InstantTimeProvider
import com.malinskiy.marathon.config.strategy.FlakinessStrategyConfiguration.ProbabilityBasedFlakinessStrategyConfiguration
import java.time.Duration
import java.time.Instant

class ProbabilityBasedFlakinessStrategyConfigurationDeserializer(private val instantTimeProvider: InstantTimeProvider) :
    StdDeserializer<ProbabilityBasedFlakinessStrategyConfiguration>(ProbabilityBasedFlakinessStrategyConfiguration::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): ProbabilityBasedFlakinessStrategyConfiguration {
        val codec = p?.codec as ObjectMapper
        val node: JsonNode = codec.readTree(p) ?: throw ConfigurationException("Invalid sorting strategy")

        val minSuccessRate = node.findValue("minSuccessRate")?.asDouble()
            ?: throw ConfigurationException("Missing minimum success rate value")
        val maxCount = node.findValue("maxCount")?.asInt()
            ?: throw ConfigurationException("Missing maximum count value")

        val timeLimitValue = node.findValue("timeLimit")
            ?: throw ConfigurationException("Missing time limit value")
        val instant = codec.treeToValueOrNull(timeLimitValue, Instant::class.java)
            ?: codec.treeToValueOrNull(timeLimitValue, Duration::class.java)?.addToInstant(instantTimeProvider.referenceTime())
            ?: throw ConfigurationException("Unable to deserialize $timeLimitValue into Instant")

        return ProbabilityBasedFlakinessStrategyConfiguration(
            minSuccessRate = minSuccessRate,
            maxCount = maxCount,
            timeLimit = instant
        )
    }
}

private fun Duration.addToInstant(instant: Instant): Instant = instant.plus(this)
private fun <T> ObjectMapper.treeToValueOrNull(node: TreeNode, clazz: Class<T>): T? {
    val result: T
    try {
        result = treeToValue(node, clazz)
    } catch (e: InvalidFormatException) {
        return null
    }
    return result
}
