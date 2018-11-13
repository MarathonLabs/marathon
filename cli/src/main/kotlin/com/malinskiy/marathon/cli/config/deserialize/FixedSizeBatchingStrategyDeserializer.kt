package com.malinskiy.marathon.cli.config.deserialize

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.malinskiy.marathon.cli.config.time.InstantTimeProvider
import com.malinskiy.marathon.exceptions.ConfigurationException
import com.malinskiy.marathon.execution.strategy.impl.batching.FixedSizeBatchingStrategy
import java.time.Duration
import java.time.Instant

class FixedSizeBatchingStrategyDeserializer(private val instantTimeProvider: InstantTimeProvider):
        StdDeserializer<FixedSizeBatchingStrategy>(FixedSizeBatchingStrategy::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): FixedSizeBatchingStrategy {
        val codec = p?.codec as ObjectMapper
        val node: JsonNode = codec.readTree(p) ?: throw ConfigurationException("Invalid sorting strategy")

        val size = node.findValue("percentile")?.asInt()
                ?: throw ConfigurationException("Missing size value")

        val durationMillis = node.findValue("durationMillis")?.asLong()
                ?: throw ConfigurationException("Missing durationMillis value")

        val percentile = node.findValue("percentile")?.asDouble()
                ?: throw ConfigurationException("Missing percentile value")

        val timeLimitValue = node.findValue("timeLimit")
                ?: throw ConfigurationException("Missing time limit value")
        val instant = codec.treeToValueOrNull(timeLimitValue, Instant::class.java)
                ?: codec.treeToValueOrNull(timeLimitValue, Duration::class.java)?.
                        addToInstant(instantTimeProvider.referenceTime())
                ?: throw ConfigurationException("Can't deserialize timeLimit")

        return FixedSizeBatchingStrategy(size, durationMillis, percentile, instant)
    }
}

private fun Duration.addToInstant(instant: Instant): Instant = instant.plus(this)
private fun <T> ObjectMapper.treeToValueOrNull(node: TreeNode, clazz: Class<T>): T? {
    val result: T
    try {
        result = treeToValue(node, clazz)
    } catch (e: InvalidFormatException) { return null }
    return result
}
