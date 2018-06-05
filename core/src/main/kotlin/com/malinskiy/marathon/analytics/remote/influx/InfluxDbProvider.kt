package com.malinskiy.marathon.analytics.remote.influx

import org.influxdb.BatchOptions
import org.influxdb.InfluxDBFactory

const val dbName = "tests"

object InfluxDbProvider {
    val influxDb = InfluxDBFactory.connect("http://localhost:8086", "root", "root")!!

    init {
        if (!influxDb.databaseExists(dbName)) {
            influxDb.createDatabase(dbName)
        }
        influxDb.setDatabase(dbName)
        val rpName = "aRetentionPolicy"
        influxDb.createRetentionPolicy(rpName, dbName, "30d", "30m", 2, true)
        influxDb.setRetentionPolicy(rpName)

        influxDb.enableBatch(BatchOptions.DEFAULTS)
    }
}