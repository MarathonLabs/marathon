package com.malinskiy.marathon.gradle

import com.malinskiy.marathon.config.AnalyticsConfiguration
import com.malinskiy.marathon.config.analytics.Defaults
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.util.internal.ConfigureUtil

class AnalyticsConfig {
    var influx: InfluxConfig? = null
    var graphite: GraphiteConfig? = null
    var influx2: Influx2Config? = null

    fun influx(action: Action<InfluxConfig>) {
        influx = InfluxConfig().also { action.execute(it) }
    }

    fun influx(closure: Closure<InfluxConfig>) = influx(ConfigureUtil.configureUsing(closure))

    fun graphite(action: Action<GraphiteConfig>) {
        graphite = GraphiteConfig().also { action.execute(it) }
    }

    fun graphite(closure: Closure<GraphiteConfig>) = graphite(ConfigureUtil.configureUsing(closure))

    fun influx2(action: Action<Influx2Config>) {
        influx2 = Influx2Config().also { action.execute(it) }
    }

    fun influx2(closure: Closure<Influx2Config>) = influx2(ConfigureUtil.configureUsing(closure))
}

class InfluxConfig {
    var url: String = ""
    var user: String = ""
    var password: String = ""
    var dbName: String = ""
    var retentionPolicy: RetentionPolicy? = null
    var defaults: Defaults = Defaults()
}

class Influx2Config {
    var url: String = ""
    var token: String = ""
    var organization: String = ""
    var bucket: String = ""
    var retentionPolicy: Influx2RetentionPolicy? = null
    var defaults: Defaults = Defaults()
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
    var defaults: Defaults = Defaults()
}

fun AnalyticsConfig.toAnalyticsConfiguration(): AnalyticsConfiguration {
    val influx = this.influx
    val influx2 = this.influx2
    val graphite = this.graphite
    return when {
        influx2 != null -> AnalyticsConfiguration.InfluxDb2Configuration(
            url = influx2.url,
            token = influx2.token,
            organization = influx2.organization,
            bucket = influx2.bucket,
            retentionPolicyConfiguration = influx2.retentionPolicy?.toRetentionPolicy()
                ?: AnalyticsConfiguration.InfluxDb2Configuration.RetentionPolicyConfiguration.default,
            defaults = influx2.defaults
        )
        influx != null -> AnalyticsConfiguration.InfluxDbConfiguration(
            dbName = influx.dbName,
            user = influx.user,
            password = influx.password,
            url = influx.url,
            retentionPolicyConfiguration = influx.retentionPolicy?.toRetentionPolicy()
                ?: AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration.default,
            defaults = influx.defaults
        )
        graphite != null -> AnalyticsConfiguration.GraphiteConfiguration(
            host = graphite.host,
            port = graphite.port?.toIntOrNull(),
            prefix = graphite.prefix,
            defaults = graphite.defaults
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
