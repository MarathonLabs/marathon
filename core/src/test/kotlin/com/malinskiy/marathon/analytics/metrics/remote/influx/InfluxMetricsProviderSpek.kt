package com.malinskiy.marathon.analytics.metrics.remote.influx

import com.malinskiy.marathon.test.Test
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.shouldEqual
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBIOException
import org.influxdb.dto.QueryResult
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.io.IOException
import java.time.Instant

class InfluxMetricsProviderSpek : Spek({
    describe("InfluxMetricsProvider") {
        it("should return actual value of success rate") {
            val influxDb = mock<InfluxDB>()
            val provider = InfluxMetricsProvider(influxDb, "test")

            val queryResult = QueryResult().apply {
                results = listOf(
                        QueryResult.Result().apply {
                            series = listOf(
                                    QueryResult.Series().apply {
                                        name = "tests"
                                        columns = listOf("time", "mean")
                                        tags = mapOf<String, String>("testname" to "com.example.SingleTest.method")
                                        values = listOf(
                                                listOf("2019-08-24T08:25:09.219Z", 0.5)
                                        )
                                    }
                            )
                        }
                )
            }
            whenever(influxDb.query(any())).thenReturn(queryResult)

            val rate = provider.successRate(Test("com.example", "SingleTest", "method", emptyList()), Instant.now())

            rate shouldEqual 0.5
        }

        it("should return actual value of execution time") {
            val influxDb = mock<InfluxDB>()
            val provider = InfluxMetricsProvider(influxDb, "test")

            val queryResult = QueryResult().apply {
                results = listOf(
                        QueryResult.Result().apply {
                            series = listOf(
                                    QueryResult.Series().apply {
                                        name = "tests"
                                        columns = listOf("time", "percentile")
                                        tags = mapOf<String, String>("testname" to "com.example.SingleTest.method")
                                        values = listOf(
                                                listOf("2019-08-24T08:25:09.219Z", 5000.0)
                                        )
                                    }
                            )
                        }
                )
            }
            whenever(influxDb.query(any())).thenReturn(queryResult)

            val rate = provider.executionTime(Test("com.example", "SingleTest", "method", emptyList()), 99.0, Instant.now())

            rate shouldEqual 5000.0
        }


        it("should return default success rate if initialization failed") {
            val influxDb = mock<InfluxDB>()
            val provider = InfluxMetricsProvider(influxDb, "test")

            whenever(influxDb.query(any())).thenThrow(InfluxDBIOException(IOException("test exception")))

            val rate = provider.successRate(Test("com.example", "SingleTest", "method", emptyList()), Instant.now())

            rate shouldEqual 0.0
        }

        it("should return default execution time if initialization failed") {
            val influxDb = mock<InfluxDB>()
            val provider = InfluxMetricsProvider(influxDb, "test")

            whenever(influxDb.query(any())).thenThrow(InfluxDBIOException(IOException("test exception")))

            val executionTime = provider.executionTime(Test("com.example", "SingleTest", "method", emptyList()), 95.0, Instant.now())

            executionTime shouldEqual 300_000.0
        }
    }
})