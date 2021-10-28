package com.malinskiy.marathon.config

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = AnalyticsConfiguration.DisabledAnalytics::class, name = "disabled"),
    JsonSubTypes.Type(value = AnalyticsConfiguration.InfluxDbConfiguration::class, name = "influxdb"),
    JsonSubTypes.Type(value = AnalyticsConfiguration.GraphiteConfiguration::class, name = "graphite"),
)
sealed class AnalyticsConfiguration {
    object DisabledAnalytics : AnalyticsConfiguration()
    data class InfluxDbConfiguration(
        val url: String,
        val user: String,
        val password: String,
        val dbName: String,
        val retentionPolicyConfiguration: RetentionPolicyConfiguration
    ) : AnalyticsConfiguration() {
        data class RetentionPolicyConfiguration(
            val name: String,
            val duration: String,
            val shardDuration: String,
            val replicationFactor: Int,
            val isDefault: Boolean
        ) {
            companion object {
                val default: RetentionPolicyConfiguration = RetentionPolicyConfiguration("rpMarathon", "30d", "30m", 2, true)
            }
        }
    }

    data class GraphiteConfiguration(
        val host: String,
        val port: Int?,
        val prefix: String?
    ) : AnalyticsConfiguration()
}
