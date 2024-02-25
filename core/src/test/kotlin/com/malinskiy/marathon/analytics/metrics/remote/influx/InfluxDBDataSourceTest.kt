package com.malinskiy.marathon.analytics.metrics.remote.influx

import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldEqualTo
import org.influxdb.InfluxDB
import org.influxdb.dto.Query
import org.influxdb.dto.QueryResult
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import java.time.Instant


class InfluxDBDataSourceTest {
    @Test
    fun shouldReturnActualValueOfSuccessRate() {
        val influxDb = mock<InfluxDB>()
        val provider = InfluxDBDataSource(influxDb, "test", "rpMarathon")

        val queryResult = QueryResult().apply {
            results = listOf(
                QueryResult.Result().apply {
                    series = listOf(
                        QueryResult.Series().apply {
                            name = "tests"
                            columns = listOf("time", "mean")
                            tags =
                                mapOf<String, String>("testname" to "com.example.SingleTest.method")
                            values = listOf(
                                listOf("2019-08-24T08:25:09.219Z", 0.5)
                            )
                        }
                    )
                }
            )
        }
        whenever(influxDb.query(any())).thenReturn(queryResult)

        val rate = provider.requestAllSuccessRates(Instant.now())

        rate.size shouldEqualTo 1
        rate[0].mean shouldBeEqualTo 0.5
        rate[0].testName shouldBeEqualTo "com.example.SingleTest.method"
    }

    @Test
    fun shouldReturnActualValueOfExecutionTime() {
        val influxDb = mock<InfluxDB>()
        val provider = InfluxDBDataSource(influxDb, "test", "rpMarathon")

        val queryResult = QueryResult().apply {
            results = listOf(
                QueryResult.Result().apply {
                    series = listOf(
                        QueryResult.Series().apply {
                            name = "tests"
                            columns = listOf("time", "percentile")
                            tags =
                                mapOf<String, String>("testname" to "com.example.SingleTest.method")
                            values = listOf(
                                listOf("2019-08-24T08:25:09.219Z", 5000.0)
                            )
                        }
                    )
                }
            )
        }
        whenever(influxDb.query(any())).thenReturn(queryResult)

        val rate = provider.requestAllExecutionTimes(99.0, Instant.now())

        rate.size shouldEqual 1
        rate[0].testName shouldBeEqualTo "com.example.SingleTest.method"
        rate[0].percentile shouldBeEqualTo 5000.0
    }

    @Test
    fun shouldReturnActualValueOfSuccessRateWithCustomRetentionPolicy() {
        val influxDb = mock<InfluxDB>()
        val provider = InfluxDBDataSource(influxDb, "test", "blah")

        val queryResult = QueryResult().apply {
            results = listOf(
                QueryResult.Result().apply {
                    series = listOf(
                        QueryResult.Series().apply {
                            name = "tests"
                            columns = listOf("time", "mean")
                            tags =
                                mapOf<String, String>("testname" to "com.example.SingleTest.method")
                            values = listOf(
                                listOf("2019-08-24T08:25:09.219Z", 0.5)
                            )
                        }
                    )
                }
            )
        }

        val query = Query(
            """
                        SELECT MEAN("success")
                        FROM "blah"."tests"
                        WHERE time >= '2019-08-24T08:25:09.219Z'
                        GROUP BY "testname"
                    """.trimIndent(), "test"
        )

        whenever(influxDb.query(eq(query))).thenReturn(queryResult)

        val rate = provider.requestAllSuccessRates(Instant.parse("2019-08-24T08:25:09.219Z"))

        rate.size shouldEqualTo 1
        rate[0].mean shouldBeEqualTo 0.5
        rate[0].testName shouldBeEqualTo "com.example.SingleTest.method"
    }

    @Test
    fun shouldReturnActualValueOfExecutionTimeWithCustomRetentionPolicy() {
        val influxDb = mock<InfluxDB>()
        val provider = InfluxDBDataSource(influxDb, "test", "blah")

        val queryResult = QueryResult().apply {
            results = listOf(
                QueryResult.Result().apply {
                    series = listOf(
                        QueryResult.Series().apply {
                            name = "tests"
                            columns = listOf("time", "percentile")
                            tags =
                                mapOf<String, String>("testname" to "com.example.SingleTest.method")
                            values = listOf(
                                listOf("2019-08-24T08:25:09.219Z", 5000.0)
                            )
                        }
                    )
                }
            )
        }

        val query = Query(
            """
                        SELECT PERCENTILE("duration",99.0)
                        FROM "blah"."tests"
                        WHERE time >= '2019-08-24T08:25:09.219Z'
                        GROUP BY "testname"
                    """.trimIndent(), "test"
        )
        whenever(influxDb.query(eq(query))).thenReturn(queryResult)

        val rate = provider.requestAllExecutionTimes(99.0, Instant.parse("2019-08-24T08:25:09.219Z"))

        rate.size shouldEqual 1
        rate[0].testName shouldBeEqualTo "com.example.SingleTest.method"
        rate[0].percentile shouldBeEqualTo 5000.0
    }
}
