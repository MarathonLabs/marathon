package com.malinskiy.marathon.gradle

import com.malinskiy.marathon.config.AnalyticsConfiguration
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.kotlin.dsl.invoke

class AnalyticsConfig {
    var influx: InfluxConfig? = null
    var graphite: GraphiteConfig? = null
    var influx2: Influx2Config = Influx2Config()

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

    fun influx2(block: Action<Influx2Config>) {
        block.invoke(influx2)
    }
}

class InfluxConfig {
    var url: String = ""
    var user: String = ""
    var password: String = ""
    var dbName: String = ""
    var retentionPolicy: RetentionPolicy? = null
}

class Influx2Config {
    var url: String = ""
    var token: String = ""
    var organization: String = ""
    var bucket: String = ""
    var retentionPolicy: Influx2RetentionPolicy? = null
}

class Influx2RetentionPolicy {
    var everySeconds: Int = 86400 * 30
    var shardGroupDurationSeconds: Long = 0L
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
        influx2 != null -> AnalyticsConfiguration.InfluxDb2Configuration(
            url = influx2.url,
            token = influx2.token,
            organization = influx2.organization,
            bucket = influx2.bucket,
            retentionPolicyConfiguration = influx2.retentionPolicy?.toRetentionPolicy()
                ?: AnalyticsConfiguration.InfluxDb2Configuration.RetentionPolicyConfiguration.default
        )
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

private fun Influx2RetentionPolicy.toRetentionPolicy(): AnalyticsConfiguration.InfluxDb2Configuration.RetentionPolicyConfiguration {
    return AnalyticsConfiguration.InfluxDb2Configuration.RetentionPolicyConfiguration(
        everySeconds = everySeconds,
        shardGroupDurationSeconds = shardGroupDurationSeconds,
    )
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
