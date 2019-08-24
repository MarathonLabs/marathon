package com.malinskiy.marathon.analytics.metrics.remote.influx

import com.malinskiy.marathon.test.Test
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.shouldEqual
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBIOException
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.io.IOException
import java.time.Instant

class InfluxMetricsProviderSpek: Spek({
    describe("InfluxMetricsProvider") {
        it("should return default if initialization failed") {
            val influxDb = mock<InfluxDB>()
            val provider = InfluxMetricsProvider(influxDb, "test")

            whenever(influxDb.query(any())).thenThrow(InfluxDBIOException(IOException("test exception")))

            val rate = provider.successRate(Test("com.example", "SingleTest", "method", emptyList()), Instant.now())

            rate shouldEqual 0.0
        }
    }
})