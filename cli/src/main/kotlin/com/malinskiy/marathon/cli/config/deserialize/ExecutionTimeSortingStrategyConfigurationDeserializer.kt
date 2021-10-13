package com.malinskiy.marathon.cli.config.deserialize

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.malinskiy.marathon.cli.config.time.InstantTimeProvider
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.strategy.SortingStrategyConfiguration
import java.time.Duration
import java.time.Instant

class ExecutionTimeSortingStrategyConfigurationDeserializer(private val instantTimeProvider: InstantTimeProvider) :
    StdDeserializer<SortingStrategyConfiguration.ExecutionTimeSortingStrategyConfiguration>(SortingStrategyConfiguration.ExecutionTimeSortingStrategyConfiguration::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): SortingStrategyConfiguration.ExecutionTimeSortingStrategyConfiguration {
        val codec = p?.codec as ObjectMapper
        val node: JsonNode = codec.readTree(p) ?: throw ConfigurationException("Invalid sorting strategy")

        val percentile = node.findValue("percentile")?.asDouble()
            ?: throw ConfigurationException("Missing percentile value")

        val timeLimitValue = node.findValue("timeLimit")
            ?: throw ConfigurationException("Missing time limit value")
        val instant = codec.treeToValueOrNull(timeLimitValue, Instant::class.java)
            ?: codec.treeToValueOrNull(timeLimitValue, Duration::class.java)?.addToInstant(instantTimeProvider.referenceTime())
            ?: throw ConfigurationException("bbb")

        return SortingStrategyConfiguration.ExecutionTimeSortingStrategyConfiguration(percentile, instant)
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
