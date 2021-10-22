package com.malinskiy.marathon.config.serialization.yaml

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.malinskiy.marathon.config.AnalyticsConfiguration
import com.malinskiy.marathon.config.exceptions.ConfigurationException

class GraphiteConfigurationDeserializer
    : StdDeserializer<AnalyticsConfiguration.GraphiteConfiguration>(
    AnalyticsConfiguration.GraphiteConfiguration::class.java
) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): AnalyticsConfiguration.GraphiteConfiguration {
        val node: JsonNode? = p?.codec?.readTree(p)

        val host = node?.get("host")?.asText()
        val portString = node?.get("port")?.asText()
        val prefix = node?.get("prefix")?.asText()

        val port = portString?.toIntOrNull()

        if (host == null) throw ConfigurationException("GraphiteConfigurationDeserializer: host should be specified")
        if (portString != null && port == null) throw ConfigurationException("GraphiteConfigurationDeserializer: port should be a number, e.g. 2003")
        if (prefix?.isEmpty() == true) throw ConfigurationException("GraphiteConfigurationDeserializer: prefix cannot be empty")

        return AnalyticsConfiguration.GraphiteConfiguration(host, port, prefix)
    }
}
