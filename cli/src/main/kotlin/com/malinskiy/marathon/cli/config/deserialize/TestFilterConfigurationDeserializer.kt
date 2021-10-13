package com.malinskiy.marathon.cli.config.deserialize

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.malinskiy.marathon.config.TestFilterConfiguration
import com.malinskiy.marathon.config.exceptions.ConfigurationException

class TestFilterConfigurationDeserializer : StdDeserializer<TestFilterConfiguration>(TestFilterConfiguration::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): TestFilterConfiguration {
        val codec = p?.codec as ObjectMapper
        val node: JsonNode = codec.readTree(p) ?: throw ConfigurationException("Missing filter strategy")
        val type = node.get("type").asText()

        return when (type) {
            "simple-class-name" -> {
                (node as ObjectNode).remove("type")
                codec.treeToValue<TestFilterConfiguration.SimpleClassnameFilterConfiguration>(node) ?: throw ConfigurationException("Missing filter strategy")
            }
            "fully-qualified-class-name" -> {
                (node as ObjectNode).remove("type")
                codec.treeToValue<TestFilterConfiguration.FullyQualifiedClassnameFilterConfiguration>(node) ?: throw ConfigurationException("Missing filter strategy")
            }
            "fully-qualified-test-name" -> {
                (node as ObjectNode).remove("type")
                codec.treeToValue<TestFilterConfiguration.FullyQualifiedTestnameFilterConfiguration>(node) ?: throw ConfigurationException("Missing filter strategy")
            }
            "package" -> {
                (node as ObjectNode).remove("type")
                codec.treeToValue<TestFilterConfiguration.TestPackageFilterConfiguration>(node) ?: throw ConfigurationException("Missing filter strategy")
            }
            "annotation" -> {
                (node as ObjectNode).remove("type")
                codec.treeToValue<TestFilterConfiguration.AnnotationFilterConfiguration>(node) ?: throw ConfigurationException("Missing filter strategy")
            }
            "method" -> {
                (node as ObjectNode).remove("type")
                codec.treeToValue<TestFilterConfiguration.TestMethodFilterConfiguration>(node) ?: throw ConfigurationException("Missing filter strategy")
            }
            "composition" -> {
                (node as ObjectNode).remove("type")
                codec.treeToValue<TestFilterConfiguration.CompositionFilterConfiguration>(node) ?: throw ConfigurationException("Missing filter strategy")
            }
            "annotationData" -> {
                (node as ObjectNode).remove("type")
                codec.treeToValue<TestFilterConfiguration.AnnotationDataFilterConfiguration>(node) ?: throw ConfigurationException("Missing filter strategy")
            }
            "fragmentation" -> {
                (node as ObjectNode).remove("type")
                codec.treeToValue<TestFilterConfiguration.FragmentationFilterConfiguration>(node) ?: throw ConfigurationException("Missing filter strategy")
            }

            else -> throw ConfigurationException("Unrecognized filter type $type")
        }
    }
}
