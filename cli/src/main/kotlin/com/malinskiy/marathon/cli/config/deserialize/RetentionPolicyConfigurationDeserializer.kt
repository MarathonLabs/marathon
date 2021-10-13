package com.malinskiy.marathon.cli.config.deserialize

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.malinskiy.marathon.config.AnalyticsConfiguration
import com.malinskiy.marathon.config.exceptions.ConfigurationException

class RetentionPolicyConfigurationDeserializer
    : StdDeserializer<AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration>(
    AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration::class.java
) {
    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?
    ): AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration {
        val node: JsonNode? = p?.codec?.readTree(p)
        val name = node?.get("name")?.asText()
        val duration = node?.get("duration")?.asText()
        val shardDuration = node?.get("shardDuration")?.asText()
        val replicationFactor = node?.get("replicationFactor")?.asInt()
        val isDefault = node?.get("isDefault")?.asBoolean()

        if (name == null) throw ConfigurationException("RetentionPolicyConfigurationDeserializer: name should be specified")
        if (duration == null) throw ConfigurationException("RetentionPolicyConfigurationDeserializer: duration should be specified")
        if (shardDuration == null) throw ConfigurationException("RetentionPolicyConfigurationDeserializer: shardDuration should be specified")
        if (replicationFactor == null) throw ConfigurationException("RetentionPolicyConfigurationDeserializer: replicationFactor should be specified")
        if (isDefault == null) throw ConfigurationException("RetentionPolicyConfigurationDeserializer: isDefault should be specified")

        return AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration(
            name,
            duration,
            shardDuration,
            replicationFactor,
            isDefault
        )
    }
}
