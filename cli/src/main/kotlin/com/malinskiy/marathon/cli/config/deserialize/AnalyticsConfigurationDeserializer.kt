package com.malinskiy.marathon.cli.config.deserialize

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.malinskiy.marathon.execution.AnalyticsConfiguration


class AnalyticsConfigurationDeserializer : StdDeserializer<AnalyticsConfiguration>(AnalyticsConfiguration::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): AnalyticsConfiguration {
        val node: JsonNode? = p?.codec?.readTree(p)


        val influxNode = node?.get("influx")
        influxNode?.let {
            val influxDbConfiguration =
                ctxt?.readValue(influxNode.traverse(p.codec), AnalyticsConfiguration.InfluxDbConfiguration::class.java)
            if (influxDbConfiguration != null) return influxDbConfiguration
        }

        return AnalyticsConfiguration.DisabledAnalytics
    }
}
