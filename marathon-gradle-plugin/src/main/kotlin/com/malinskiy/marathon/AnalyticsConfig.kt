package com.malinskiy.marathon

import com.malinskiy.marathon.execution.AnalyticsConfiguration
import com.malinskiy.marathon.execution.AnalyticsConfiguration.DisabledAnalytics
import com.malinskiy.marathon.execution.AnalyticsConfiguration.GraphiteConfiguration
import com.malinskiy.marathon.execution.AnalyticsConfiguration.InfluxDbConfiguration
import com.malinskiy.marathon.execution.AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration
import org.gradle.api.Action
import java.io.Serializable

class AnalyticsConfig : Serializable {
    var influx: InfluxConfig? = null
    var graphite: GraphiteConfig? = null

    fun influx(action: Action<InfluxConfig>) {
        influx = InfluxConfig().also(action::execute)
    }

    fun graphite(action: Action<GraphiteConfig>) {
        graphite = GraphiteConfig().also(action::execute)
    }
}

class InfluxConfig : Serializable {
    var url: String = ""
    var user: String = ""
    var password: String = ""
    var dbName: String = ""
    var retentionPolicy: RetentionPolicy? = null
}

class RetentionPolicy : Serializable {
    var name: String = "rpMarathon"
    var duration: String = "30d"
    var shardDuration: String = "30m"
    var replicationFactor: Int = 2
    var isDefault: Boolean = true
}

class GraphiteConfig : Serializable {
    var host: String = ""
    var port: String? = null
    var prefix: String? = null
}

fun AnalyticsConfig.toAnalyticsConfiguration(): AnalyticsConfiguration {
    val influx = this.influx
    val graphite = this.graphite
    return when {
        influx != null -> InfluxDbConfiguration(
            dbName = influx.dbName,
            user = influx.user,
            password = influx.password,
            url = influx.url,
            retentionPolicyConfiguration = influx.retentionPolicy?.toRetentionPolicy()
                ?: RetentionPolicyConfiguration.default
        )
        graphite != null -> GraphiteConfiguration(
            host = graphite.host,
            port = graphite.port?.toIntOrNull(),
            prefix = graphite.prefix
        )
        else -> DisabledAnalytics
    }
}

private fun RetentionPolicy.toRetentionPolicy(): RetentionPolicyConfiguration {
    return RetentionPolicyConfiguration(name, duration, shardDuration, replicationFactor, isDefault)
}
