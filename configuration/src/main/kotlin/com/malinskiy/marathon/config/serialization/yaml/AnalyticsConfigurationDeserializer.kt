package com.malinskiy.marathon.config.serialization.yaml

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.malinskiy.marathon.config.AnalyticsConfiguration
import com.malinskiy.marathon.config.exceptions.ConfigurationException


class AnalyticsConfigurationDeserializer : StdDeserializer<AnalyticsConfiguration>(AnalyticsConfiguration::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): AnalyticsConfiguration {
        val node: JsonNode? = p?.codec?.readTree(p)

        val influxNode = node?.get("influx")
        val graphiteNode = node?.get("graphite")

        if (influxNode != null && graphiteNode != null) {
            throw ConfigurationException("You cannot use both InfluxDB and Graphite a the same time")
        }

        if (influxNode != null) {
            val influxDbConfiguration =
                ctxt?.readValue(influxNode.traverse(p.codec), AnalyticsConfiguration.InfluxDbConfiguration::class.java)
            if (influxDbConfiguration != null) return influxDbConfiguration
        }

        if (graphiteNode != null) {
            val graphiteConfiguration =
                ctxt?.readValue(graphiteNode.traverse(p.codec), AnalyticsConfiguration.GraphiteConfiguration::class.java)
            if (graphiteConfiguration != null) return graphiteConfiguration
        }

        return AnalyticsConfiguration.DisabledAnalytics
    }
}
