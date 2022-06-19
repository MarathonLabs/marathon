package com.malinskiy.marathon.analytics.external.influx

import com.malinskiy.marathon.config.AnalyticsConfiguration
import okhttp3.OkHttpClient
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBFactory
import java.util.concurrent.TimeUnit

const val TIMEOUT_SEC = 60L

class InfluxDbProvider(configuration: AnalyticsConfiguration.InfluxDbConfiguration, private val debug: Boolean = false) {

    private val url = configuration.url
    private val user = configuration.user
    private val password = configuration.password
    private val dbName = configuration.dbName
    private val retentionPolicyConfiguration = configuration.retentionPolicyConfiguration

    fun createDb(): InfluxDB {
        val okHttpBuilder = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)

        val influxDb = if (user.isNotEmpty() && password.isNotEmpty()) {
            InfluxDBFactory.connect(url, user, password, okHttpBuilder)
        } else {
            InfluxDBFactory.connect(url, okHttpBuilder)
        }
        
        if(debug) {
            influxDb.setLogLevel(InfluxDB.LogLevel.BASIC)
        } else {
            influxDb.setLogLevel(InfluxDB.LogLevel.NONE)
        }

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
