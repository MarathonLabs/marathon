package com.malinskiy.marathon.cli.config.deserialize

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.malinskiy.marathon.android.configuration.TestParserConfiguration
import com.malinskiy.marathon.exceptions.ConfigurationException


class TestParserConfigurationDeserializer : StdDeserializer<TestParserConfiguration>(TestParserConfiguration::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): TestParserConfiguration {
        val codec = p?.codec as ObjectMapper
        val node: JsonNode = codec.readTree(p) ?: throw ConfigurationException("Missing test parser configuration")
        val type = node.get("type").asText()

        return when (type) {
            "local" -> TestParserConfiguration.LocalTestParser
            "remote" -> {
                (node as ObjectNode).remove("type")
                return codec.treeToValue<TestParserConfiguration.RemoteTestParser>(node)
                    ?: throw ConfigurationException("Missing test parser configuration")
            }
            else -> throw ConfigurationException("Unrecognized test parser configuration $type")
        }
    }
}
