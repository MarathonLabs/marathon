package com.malinskiy.marathon.analytics.tracker.remote.influx

import com.malinskiy.marathon.execution.AnalyticsConfiguration
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBFactory

class InfluxDbProvider(configuration: AnalyticsConfiguration.InfluxDbConfiguration) {

    private val url = configuration.url
    private val user = configuration.user
    private val password = configuration.password
    private val dbName = configuration.dbName
    private val retentionPolicyConfiguration = configuration.retentionPolicyConfiguration

    fun createDb(): InfluxDB {
        val influxDb = if (user.isNotEmpty() && password.isNotEmpty()) {
            InfluxDBFactory.connect(url, user, password)
        } else {
            InfluxDBFactory.connect(url)
        }
        influxDb.setLogLevel(InfluxDB.LogLevel.BASIC)

        val rpName = retentionPolicyConfiguration.name
        if (!influxDb.databaseExists(dbName)) {
            influxDb.createDatabase(dbName)

            val duration = retentionPolicyConfiguration.duration
            val shardDuration = retentionPolicyConfiguration.shardDuration
            val replicationFactor = retentionPolicyConfiguration.replicationFactor
            val isDefault = retentionPolicyConfiguration.isDefault

            influxDb.createRetentionPolicy(rpName, dbName, duration, shardDuration, replicationFactor, isDefault)
        }

        influxDb.setDatabase(dbName)
        influxDb.setRetentionPolicy(rpName)
        influxDb.enableBatch()
        return influxDb
    }
}
