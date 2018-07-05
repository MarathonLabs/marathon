package com.malinskiy.marathon

import groovy.lang.Closure
import com.malinskiy.marathon.execution.AnalyticsConfiguration
import com.malinskiy.marathon.execution.AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration

class AnalyticsConfig {
    var influx: InfluxConfig? = null

    fun influx(closure: Closure<*>) {
        influx = InfluxConfig()
        closure.delegate = influx
        closure.call()
    }

    fun influx(block: InfluxConfig.() -> Unit) {
        influx = InfluxConfig().also(block)
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

fun AnalyticsConfig.toAnalyticsConfiguration(): AnalyticsConfiguration {
    return influx?.let {
        AnalyticsConfiguration.InfluxDbConfiguration(
                dbName = it.dbName,
                user = it.user,
                password = it.password,
                url = it.url,
                retentionPolicyConfiguration = it.retentionPolicy?.toRetentionPolicy()
                        ?: RetentionPolicyConfiguration.default)
    } ?: AnalyticsConfiguration.DisabledAnalytics
}

private fun RetentionPolicy.toRetentionPolicy(): RetentionPolicyConfiguration {
    return RetentionPolicyConfiguration(name, duration, shardDuration, replicationFactor, isDefault)
}