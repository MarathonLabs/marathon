package com.malinskiy.marathon.analytics.metrics.remote.influx

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldEqualTo
import org.influxdb.InfluxDB
import org.influxdb.dto.QueryResult
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.time.Instant


class InfluxDBDataSourceSpec : Spek(
    {
        describe("InfluxMetricsProvider") {
            it("should return actual value of success rate") {
                val influxDb = mock<InfluxDB>()
                val provider = InfluxDBDataSource(influxDb, "test")

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
                rate[0].mean shouldEqualTo 0.5
                rate[0].testName shouldBeEqualTo "com.example.SingleTest.method"
            }

            it("should return actual value of execution time") {
                val influxDb = mock<InfluxDB>()
                val provider = InfluxDBDataSource(influxDb, "test")

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
                rate[0].percentile shouldEqualTo 5000.0
            }
        }
    })