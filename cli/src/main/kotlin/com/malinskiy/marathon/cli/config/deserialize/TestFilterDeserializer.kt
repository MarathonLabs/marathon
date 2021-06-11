package com.malinskiy.marathon.cli.config.deserialize

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.malinskiy.marathon.exceptions.ConfigurationException
import com.malinskiy.marathon.execution.AnnotationDataFilter
import com.malinskiy.marathon.execution.AnnotationFilter
import com.malinskiy.marathon.execution.CompositionFilter
import com.malinskiy.marathon.execution.FullyQualifiedClassnameFilter
import com.malinskiy.marathon.execution.SimpleClassnameFilter
import com.malinskiy.marathon.execution.TestFilter
import com.malinskiy.marathon.execution.TestMethodFilter
import com.malinskiy.marathon.execution.TestPackageFilter
import com.malinskiy.marathon.execution.filter.FragmentationFilter
import com.malinskiy.marathon.execution.filter.FullyQualifiedTestnameFilter

class TestFilterDeserializer : StdDeserializer<TestFilter>(TestFilter::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): TestFilter {
        val codec = p?.codec as ObjectMapper
        val node: JsonNode = codec.readTree(p) ?: throw ConfigurationException("Missing filter strategy")
        val type = node.get("type").asText()

        return when (type) {
            "simple-class-name" -> {
                (node as ObjectNode).remove("type")
                codec.treeToValue<SimpleClassnameFilter>(node) ?: throw ConfigurationException("Missing filter strategy")
            }
            "fully-qualified-class-name" -> {
                (node as ObjectNode).remove("type")
                codec.treeToValue<FullyQualifiedClassnameFilter>(node) ?: throw ConfigurationException("Missing filter strategy")
            }
            "fully-qualified-test-name" -> {
                (node as ObjectNode).remove("type")
                codec.treeToValue<FullyQualifiedTestnameFilter>(node) ?: throw ConfigurationException("Missing filter strategy")
            }
            "package" -> {
                (node as ObjectNode).remove("type")
                codec.treeToValue<TestPackageFilter>(node) ?: throw ConfigurationException("Missing filter strategy")
            }
            "annotation" -> {
                (node as ObjectNode).remove("type")
                codec.treeToValue<AnnotationFilter>(node) ?: throw ConfigurationException("Missing filter strategy")
            }
            "method" -> {
                (node as ObjectNode).remove("type")
                codec.treeToValue<TestMethodFilter>(node) ?: throw ConfigurationException("Missing filter strategy")
            }
            "composition" -> {
                (node as ObjectNode).remove("type")
                codec.treeToValue<CompositionFilter>(node) ?: throw ConfigurationException("Missing filter strategy")
            }
            "annotationData" -> {
                (node as ObjectNode).remove("type")
                codec.treeToValue<AnnotationDataFilter>(node) ?: throw ConfigurationException("Missing filter strategy")
            }
            "fragmentation" -> {
                (node as ObjectNode).remove("type")
                codec.treeToValue<FragmentationFilter>(node) ?: throw ConfigurationException("Missing filter strategy")
            }

            else -> throw ConfigurationException("Unrecognized filter type $type")
        }
    }
}
