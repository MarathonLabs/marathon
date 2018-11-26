package com.malinskiy.marathon.cli.config.deserialize

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.malinskiy.marathon.exceptions.ConfigurationException
import com.malinskiy.marathon.execution.*

class TestFilterDeserializer : StdDeserializer<TestFilter>(TestFilter::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): TestFilter {
        val codec = p?.codec as ObjectMapper
        val node: JsonNode = codec.readTree(p) ?: throw ConfigurationException("Missing filter strategy")
        val type = node.get("type").asText()

        return when (type) {
            "simple-class-name" -> {
                (node as ObjectNode).remove("type")
                codec.treeToValue<SimpleClassnameFilter>(node)
            }
            "fully-qualified-class-name" -> {
                (node as ObjectNode).remove("type")
                codec.treeToValue<FullyQualifiedClassnameFilter>(node)
            }
            "package" -> {
                (node as ObjectNode).remove("type")
                codec.treeToValue<TestPackageFilter>(node)
            }
            "annotation" -> {
                (node as ObjectNode).remove("type")
                codec.treeToValue<AnnotationFilter>(node)
            }
            "method" -> {
                (node as ObjectNode).remove("type")
                codec.treeToValue<TestMethodFilter>(node)
            }
            "composition" -> {
                (node as ObjectNode).remove("type")
                codec.treeToValue<CompositionFilter>(node)
            }

            else -> throw ConfigurationException("Unrecognized filter type $type")
        }
    }
}
