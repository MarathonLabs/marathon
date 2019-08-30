package com.malinskiy.marathon.analytics.metrics.remote.influx

import com.malinskiy.marathon.analytics.metrics.remote.ExecutionTime
import com.malinskiy.marathon.analytics.metrics.remote.RemoteDataSource
import com.malinskiy.marathon.analytics.metrics.remote.SuccessRate
import org.influxdb.InfluxDB
import org.influxdb.annotation.Column
import org.influxdb.annotation.Measurement
import org.influxdb.dto.Query
import org.influxdb.impl.InfluxDBResultMapper
import java.time.Instant


class InfluxDBDataSource(private val influxDb: InfluxDB, private val dbName: String) : RemoteDataSource {

    @Measurement(name = "tests")
    class InfluxExecutionTime(@Column(name = "testname", tag = true) var testName: String? = null,
                              @Column(name = "percentile") var percentile: Double? = null)

    @Measurement(name = "tests")
    class InfluxSuccessRate(@Column(name = "testname", tag = true) var testName: String? = null,
                            @Column(name = "mean") var mean: Double? = null)


    private val mapper = InfluxDBResultMapper()

    override fun requestAllSuccessRates(limit: Instant): List<SuccessRate> {
        val results = influxDb.query(Query("""
            SELECT MEAN("success")
            FROM "tests"
            WHERE time >= '$limit'
            GROUP BY "testname"
        """, dbName))
        return mapper.toPOJO(results, InfluxSuccessRate::class.java).filter {
            it.testName != null && it.mean != null
        }.map {
            SuccessRate(it.testName!!, it.mean!!)
        }
    }

    override fun requestAllExecutionTimes(percentile: Double,
                                          limit: Instant): List<ExecutionTime> {
        val results = influxDb.query(Query("""
            SELECT PERCENTILE("duration",$percentile)
            FROM "tests"
            WHERE time >= '$limit'
            GROUP BY "testname"
        """, dbName))
        return mapper.toPOJO(results, InfluxExecutionTime::class.java).filter {
            it.testName != null && it.percentile != null
        }.map {
            ExecutionTime(it.testName!!, it.percentile!!)
        }
    }

    override fun close() {
        influxDb.close()
    }
}