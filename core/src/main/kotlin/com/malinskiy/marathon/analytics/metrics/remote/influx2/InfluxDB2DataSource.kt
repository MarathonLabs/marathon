package com.malinskiy.marathon.analytics.metrics.remote.influx2

import com.influxdb.annotations.Column
import com.influxdb.annotations.Measurement
import com.influxdb.client.InfluxDBClient
import com.malinskiy.marathon.analytics.metrics.remote.ExecutionTime
import com.malinskiy.marathon.analytics.metrics.remote.RemoteDataSource
import com.malinskiy.marathon.analytics.metrics.remote.SuccessRate
import java.time.Instant

class InfluxDB2DataSource(
    private val client: InfluxDBClient,
    private val bucket: String,
) : RemoteDataSource {
    private val queryApi by lazy { client.queryApi }

    @Measurement(name = "tests")
    class InfluxExecutionTime(
        @Column(name = "testname", tag = true) var testName: String? = null,
        @Column(name = "_value") var percentile: Double? = null
    )

    @Measurement(name = "tests")
    class InfluxSuccessRate(
        @Column(name = "testname", tag = true) var testName: String? = null,
        @Column(name = "_value") var mean: Double? = null
    )

    override fun requestAllSuccessRates(limit: Instant): List<SuccessRate> {
        val result = queryApi.query(
            """from(bucket: "$bucket")
              |> range(start: ${limit.epochSecond})
              |> filter(fn: (r) => r["_measurement"] == "tests" and r["_field"] == "success")
              |> keep(columns: ["testname", "_value"])
              |> mean()
              |> group(columns: ["testname"])
            """.trimIndent(), InfluxSuccessRate::class.java
        )
        return result.mapNotNull {
            val testName = it.testName
            val value = it.mean
            if (testName != null && value != null) {
                SuccessRate(testName, value)
            } else null
        }
    }

    override fun requestAllExecutionTimes(percentile: Double, limit: Instant): List<ExecutionTime> {
        val result = queryApi.query(
            """
            from(bucket: "$bucket")
              |> range(start: ${limit.epochSecond})
              |> filter(fn: (r) => r["_measurement"] == "tests" and r["_field"] == "duration")
              |> keep(columns: ["testname", "_value"])
              |> quantile(q: ${percentile / 100}, method: "exact_selector")
              |> group(columns: ["testname"])
            """.trimIndent(), InfluxExecutionTime::class.java
        )
        return result.mapNotNull {
            val testName = it.testName
            val value = it.percentile
            if (testName != null && value != null) {
                ExecutionTime(testName, value)
            } else null
        }
    }

    override fun close() {
        client.close()
    }
}
