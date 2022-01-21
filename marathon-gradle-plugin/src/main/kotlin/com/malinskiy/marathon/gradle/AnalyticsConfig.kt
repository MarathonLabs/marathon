package com.malinskiy.marathon.gradle

import com.malinskiy.marathon.config.AnalyticsConfiguration
import org.gradle.api.Action

class AnalyticsConfig {
    var influx: InfluxConfig? = null
    var graphite: GraphiteConfig? = null

    fun influx(action: Action<InfluxConfig>) {
        influx = InfluxConfig().also { action.execute(it) }
    }

    fun graphite(action: Action<GraphiteConfig>) {
        graphite = GraphiteConfig().also { action.execute(it) }
    }
}

class InfluxConfig {
    var url: String = ""
    var user: String = ""
    var password: String = ""
    var dbName: String = ""
    var retentionPolicy: RetentionPolicy? = null
}

class RetentionPolicy {
    var name: String = "rpMarathon"
    var duration: String = "30d"
    var shardDuration: String = "30m"
    var replicationFactor: Int = 2
    var isDefault: Boolean = true
}

class GraphiteConfig {
    var host: String = ""
    var port: String? = null
    var prefix: String? = null
}

fun AnalyticsConfig.toAnalyticsConfiguration(): AnalyticsConfiguration {
    val influx = this.influx
    val graphite = this.graphite
    return when {
        influx != null -> AnalyticsConfiguration.InfluxDbConfiguration(
            dbName = influx.dbName,
            user = influx.user,
            password = influx.password,
            url = influx.url,
            retentionPolicyConfiguration = influx.retentionPolicy?.toRetentionPolicy()
                ?: AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration.default
        )
        graphite != null -> AnalyticsConfiguration.GraphiteConfiguration(
            host = graphite.host,
            port = graphite.port?.toIntOrNull(),
            prefix = graphite.prefix
        )
        else -> AnalyticsConfiguration.DisabledAnalytics
    }
}

private fun RetentionPolicy.toRetentionPolicy(): AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration {
    return AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration(
        name,
        duration,
        shardDuration,
        replicationFactor,
        isDefault
    )
}
