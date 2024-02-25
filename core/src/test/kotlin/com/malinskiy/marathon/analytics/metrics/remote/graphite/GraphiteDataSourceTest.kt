package com.malinskiy.marathon.analytics.metrics.remote.graphite

import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldEqualTo
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import java.time.Instant

internal class GraphiteDataSourceTest {

    @Test
    fun shouldReturnActualValueOfSuccessRate() {
        val graphite = mock<QueryableGraphiteClient>()
        val provider = GraphiteDataSource(graphite, null)
        val limit = Instant.parse("2019-08-24T08:25:09.219Z")

        whenever(graphite.query(any(), eq(limit))).thenReturn(
            listOf(
                "com--example--SingleTest--method,1601884200,1602488400,31536000|50"
            )
        )

        val rate = provider.requestAllSuccessRates(limit)

        rate.size shouldEqualTo 1
        rate[0].mean shouldBeEqualTo 0.5
        rate[0].testName shouldBeEqualTo "com.example.SingleTest.method"
    }

    @Test
    fun shouldReturnActualValueOfExecutionTime() {
        val graphite = mock<QueryableGraphiteClient>()
        val provider = GraphiteDataSource(graphite, null)
        val limit = Instant.parse("2019-08-24T08:25:09.219Z")

        whenever(graphite.query(any(), eq(limit))).thenReturn(
            listOf(
                "com--example--SingleTest--method,1601884200,1602488400,31536000|5000.0"
            )
        )

        val rate = provider.requestAllExecutionTimes(99.0, limit)

        rate.size shouldEqual 1
        rate[0].testName shouldBeEqualTo "com.example.SingleTest.method"
        rate[0].percentile shouldBeEqualTo 5000.0
    }
}
