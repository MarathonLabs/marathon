package com.malinskiy.marathon.cli.config.deserialize

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.malinskiy.marathon.execution.AnalyticsConfiguration

class InfluxDbConfigurationDeserializer : StdDeserializer<AnalyticsConfiguration.InfluxDbConfiguration>(AnalyticsConfiguration.InfluxDbConfiguration::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): AnalyticsConfiguration.InfluxDbConfiguration {
        val node: JsonNode? = p?.codec?.readTree(p)

        val url = node?.get("url")?.asText()
        val user = node?.get("user")?.asText()
        val password = node?.get("password")?.asText()
        val dbName = node?.get("dbName")?.asText()

        val retentionPolicyNode = node?.get("retentionPolicyConfiguration")?.traverse(p?.codec)
        val retentionPolicyConfiguration =
                retentionPolicyNode?.let { ctxt?.readValue(retentionPolicyNode, AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration::class.java) }
                ?: AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration.default

        if(url == null) throw RuntimeException("InfluxDbConfigurationDeserializer: url should be specified")
        if(user == null) throw RuntimeException("InfluxDbConfigurationDeserializer: user should be specified")
        if(password == null) throw RuntimeException("InfluxDbConfigurationDeserializer: password should be specified")
        if(dbName == null) throw RuntimeException("InfluxDbConfigurationDeserializer: dbName should be specified")

        return AnalyticsConfiguration.InfluxDbConfiguration(url, user, password, dbName, retentionPolicyConfiguration)
    }
}