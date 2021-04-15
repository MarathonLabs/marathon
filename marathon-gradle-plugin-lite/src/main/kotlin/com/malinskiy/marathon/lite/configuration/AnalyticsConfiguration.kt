package com.malinskiy.marathon.lite.configuration

import com.malinskiy.marathon.cliconfig.proto.AnalyticsConfiguration.*
import java.io.Serializable
import com.malinskiy.marathon.cliconfig.proto.AnalyticsConfiguration as ProtoAnalyticsConfiguration

sealed class AnalyticsConfiguration : Serializable {
    object Disabled : AnalyticsConfiguration()
    data class InfluxDb(
        val url: String,
        val user: String,
        val password: String,
        val dbName: String,
        val retentionPolicy: RetentionPolicyConfiguration = RetentionPolicyConfiguration.default
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

    data class Graphite(
        val host: String,
        val port: Int?,
        val prefix: String?
    ) : AnalyticsConfiguration()
}


fun AnalyticsConfiguration.toProto(): ProtoAnalyticsConfiguration {
    val analyticsBuilder = ProtoAnalyticsConfiguration.newBuilder()
    when (this) {
        AnalyticsConfiguration.Disabled -> {
            analyticsBuilder.disabled = Disabled.getDefaultInstance()
        }
        is AnalyticsConfiguration.Graphite -> {
            val graphiteBuilder = Graphite.newBuilder()
            graphiteBuilder.host = this.host
            this.port?.let { graphiteBuilder.setPort(it) }
            graphiteBuilder.prefix = this.prefix
            analyticsBuilder.graphite = graphiteBuilder.build()
        }
        is AnalyticsConfiguration.InfluxDb -> {
            val influxBuilder = InfluxDb.newBuilder()
            influxBuilder.url = this.url
            influxBuilder.user = this.user
            influxBuilder.password = this.password
            influxBuilder.dbName = this.dbName

            val retentionPolicyBuilder = InfluxDb.RetentionPolicy.newBuilder()
            retentionPolicyBuilder.name = this.retentionPolicy.name
            retentionPolicyBuilder.duration = this.retentionPolicy.duration
            retentionPolicyBuilder.shardDuration = this.retentionPolicy.shardDuration
            retentionPolicyBuilder.replicationFactor = this.retentionPolicy.replicationFactor
            retentionPolicyBuilder.isDefault = this.retentionPolicy.isDefault

            influxBuilder.retentionPolicy = retentionPolicyBuilder.build()
        }
    }
    return analyticsBuilder.build()
}
