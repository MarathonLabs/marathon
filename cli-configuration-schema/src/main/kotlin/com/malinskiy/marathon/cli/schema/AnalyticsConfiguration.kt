package com.malinskiy.marathon.cli.schema

sealed class AnalyticsConfiguration {
    object Disabled : AnalyticsConfiguration()
    data class InfluxDb(
        val url: String,
        val user: String,
        val password: String,
        val dbName: String,
        val retentionPolicyConfiguration: RetentionPolicyConfiguration = RetentionPolicyConfiguration.default
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
