package com.malinskiy.marathon.cli.config.deserialize

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.malinskiy.marathon.exceptions.ConfigurationException
import com.malinskiy.marathon.vendor.junit4.configuration.executor.DockerExecutorConfiguration
import com.malinskiy.marathon.vendor.junit4.configuration.executor.ExecutorConfiguration
import com.malinskiy.marathon.vendor.junit4.configuration.executor.KubernetesExecutorConfiguration
import com.malinskiy.marathon.vendor.junit4.configuration.executor.LocalExecutorConfiguration

const val TYPE_LOCAL = "local"
const val TYPE_DOCKER = "docker"
const val TYPE_KUBERNETES = "kubernetes"

class FileJUnit4ExecutorConfigurationDeserializer : StdDeserializer<ExecutorConfiguration>(ExecutorConfiguration::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): ExecutorConfiguration {
        val codec = p?.codec as ObjectMapper
        val node: JsonNode = codec.readTree(p) ?: throw ConfigurationException("Missing JUnit4 executor configuration")
        val type = node.get("type").asText()

        return when (type) {
            TYPE_LOCAL -> {
                (node as ObjectNode).remove("type")
                codec.treeToValue<LocalExecutorConfiguration>(node) ?: throw ConfigurationException("Missing executor configuration")
            }
            TYPE_DOCKER -> {
                (node as ObjectNode).remove("type")
                codec.treeToValue<DockerExecutorConfiguration>(node) ?: throw ConfigurationException("Missing executor configuration")
            }
            TYPE_KUBERNETES -> {
                (node as ObjectNode).remove("type")
                codec.treeToValue<KubernetesExecutorConfiguration>(node) ?: throw ConfigurationException("Missing executor configuration")
            }
            else -> throw ConfigurationException(
                "Unrecognized executor type $type. " +
                    "Valid options are $TYPE_LOCAL, $TYPE_DOCKER and $TYPE_KUBERNETES"
            )
        }
    }
}
