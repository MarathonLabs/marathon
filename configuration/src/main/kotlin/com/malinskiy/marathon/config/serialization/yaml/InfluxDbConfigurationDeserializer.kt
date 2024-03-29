package com.malinskiy.marathon.config.serialization.yaml

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.malinskiy.marathon.config.AnalyticsConfiguration
import com.malinskiy.marathon.config.analytics.Defaults
import com.malinskiy.marathon.config.exceptions.ConfigurationException

class InfluxDbConfigurationDeserializer
    : StdDeserializer<AnalyticsConfiguration.InfluxDbConfiguration>(AnalyticsConfiguration.InfluxDbConfiguration::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): AnalyticsConfiguration.InfluxDbConfiguration {
        val node: JsonNode? = p?.codec?.readTree(p)

        val url = node?.get("url")?.asText()
        val user = node?.get("user")?.asText()
        val password = node?.get("password")?.asText()
        val dbName = node?.get("dbName")?.asText()

        val retentionPolicyNode = node?.get("retentionPolicyConfiguration")?.traverse(p.codec)
        val policyClazz = AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration::class.java
        val retentionPolicyConfiguration =
            retentionPolicyNode?.let { ctxt?.readValue(retentionPolicyNode, policyClazz) }
                ?: AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration.default

        val defaults = node?.get("defaults")?.let {
            ctxt?.readTreeAsValue(it, Defaults::class.java)
        } ?: Defaults()

        if (url == null) throw ConfigurationException("InfluxDbConfigurationDeserializer: url should be specified")
        if (user == null) throw ConfigurationException("InfluxDbConfigurationDeserializer: user should be specified")
        if (password == null) throw ConfigurationException("InfluxDbConfigurationDeserializer: password should be specified")
        if (dbName == null) throw ConfigurationException("InfluxDbConfigurationDeserializer: dbName should be specified")

        return AnalyticsConfiguration.InfluxDbConfiguration(url, user, password, dbName, retentionPolicyConfiguration, defaults)
    }
}
