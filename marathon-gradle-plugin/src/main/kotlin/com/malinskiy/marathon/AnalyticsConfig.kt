package com.malinskiy.marathon

import com.malinskiy.marathon.execution.AnalyticsConfiguration
import com.malinskiy.marathon.execution.AnalyticsConfiguration.Disabled
import com.malinskiy.marathon.execution.AnalyticsConfiguration.Graphite
import com.malinskiy.marathon.execution.AnalyticsConfiguration.Influx
import com.malinskiy.marathon.execution.AnalyticsConfiguration.Influx.RetentionPolicyConfiguration
import groovy.lang.Closure

class AnalyticsConfig {
    var influx: InfluxConfig? = null
    var graphite: GraphiteConfig? = null

    fun influx(closure: Closure<*>) {
        influx = InfluxConfig()
        closure.delegate = influx
        closure.call()
    }

    fun influx(block: InfluxConfig.() -> Unit) {
        influx = InfluxConfig().also(block)
    }

    fun graphite(closure: Closure<*>) {
        graphite = GraphiteConfig()
        closure.delegate = graphite
        closure.call()
    }

    fun graphite(block: GraphiteConfig.() -> Unit) {
        graphite = GraphiteConfig().also(block)
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
        influx != null -> Influx(
            dbName = influx.dbName,
            user = influx.user,
            password = influx.password,
            url = influx.url,
            retentionPolicyConfiguration = influx.retentionPolicy?.toRetentionPolicy()
                ?: RetentionPolicyConfiguration.default
        )
        graphite != null -> Graphite(
            host = graphite.host,
            port = graphite.port?.toIntOrNull(),
            prefix = graphite.prefix
        )
        else -> Disabled
    }
}

private fun RetentionPolicy.toRetentionPolicy(): RetentionPolicyConfiguration {
    return RetentionPolicyConfiguration(name, duration, shardDuration, replicationFactor, isDefault)
}
