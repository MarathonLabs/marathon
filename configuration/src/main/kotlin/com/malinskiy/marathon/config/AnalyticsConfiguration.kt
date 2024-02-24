package com.malinskiy.marathon.config

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.malinskiy.marathon.config.analytics.Defaults

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = AnalyticsConfiguration.DisabledAnalytics::class, name = "disabled"),
    JsonSubTypes.Type(value = AnalyticsConfiguration.InfluxDbConfiguration::class, name = "influxdb"),
    JsonSubTypes.Type(value = AnalyticsConfiguration.InfluxDb2Configuration::class, name = "influxdb2"),
    JsonSubTypes.Type(value = AnalyticsConfiguration.GraphiteConfiguration::class, name = "graphite"),
)
sealed class AnalyticsConfiguration {
    object DisabledAnalytics : AnalyticsConfiguration()
    data class InfluxDbConfiguration(
        val url: String,
        val user: String,
        val password: String,
        val dbName: String,
        val retentionPolicyConfiguration: RetentionPolicyConfiguration,
        val defaults: Defaults = Defaults(),
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

        /**
         * Hide sensitive information
         */
        override fun toString(): String {
            return "InfluxDbConfiguration(url='*****', user='*****', password='*****', dbName='$dbName', retentionPolicyConfiguration=$retentionPolicyConfiguration)"
        }
    }

    data class InfluxDb2Configuration(
        val url: String,
        val token: String,
        val organization: String,
        val bucket: String,
        val retentionPolicyConfiguration: RetentionPolicyConfiguration = RetentionPolicyConfiguration.default,
        val defaults: Defaults = Defaults(),
    ) : AnalyticsConfiguration() {
        data class RetentionPolicyConfiguration(
            val everySeconds: Int,
            val shardGroupDurationSeconds: Long,
        ) {
            companion object {
                val default: RetentionPolicyConfiguration = RetentionPolicyConfiguration(86400 * 30, 0L)
            }
        }

        override fun toString(): String {
            return "InfluxDb2Configuration(url='*****', token='*****', organization='$organization', bucket='$bucket', retentionPolicyConfiguration=$retentionPolicyConfiguration)"
        }


    }

    data class GraphiteConfiguration(
        val host: String,
        val port: Int?,
        val prefix: String?,
        val defaults: Defaults = Defaults(),
    ) : AnalyticsConfiguration()
}
