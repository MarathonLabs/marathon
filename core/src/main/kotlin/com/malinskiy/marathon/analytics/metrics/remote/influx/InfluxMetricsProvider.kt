package com.malinskiy.marathon.analytics.metrics.remote.influx

import com.malinskiy.marathon.analytics.metrics.MetricsProvider
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toSafeTestName
import org.influxdb.InfluxDB
import org.influxdb.annotation.Column
import org.influxdb.annotation.Measurement
import org.influxdb.dto.Query
import org.influxdb.impl.InfluxDBResultMapper

@Measurement(name = "tests")
class ExecutionTime(@Column(name = "percentile") var percentile: Double? = null)

@Measurement(name = "tests")
class SuccessRate(@Column(name = "mean") var mean: Double? = null)

internal class InfluxMetricsProvider(private val influxDb: InfluxDB,
                                     private val dbName: String) : MetricsProvider {
    private val mapper = InfluxDBResultMapper()

    override fun successRate(test: Test): Double {
        val results = influxDb.query(Query("SELECT MEAN(\"success\") FROM \"tests\" WHERE \"testname\" = \'${test.toSafeTestName()}\'", dbName))
        return mapper.toPOJO(results, SuccessRate::class.java).first()?.mean ?: 0.0
    }

    override fun executionTime(test: Test): Double {
        val percentile = 90
        val results = influxDb.query(Query("SELECT PERCENTILE(\"duration\",$percentile) " +
                "FROM \"tests\" WHERE \"testname\" = \'${test.toSafeTestName()}\'", dbName))
        return mapper.toPOJO(results, ExecutionTime::class.java).first()?.percentile ?: 0.0
    }
}
