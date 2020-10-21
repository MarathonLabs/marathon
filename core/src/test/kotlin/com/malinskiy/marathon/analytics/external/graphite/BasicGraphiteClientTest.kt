package com.malinskiy.marathon.analytics.external.graphite

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Test

class BasicGraphiteClientTest {
    private val host = "host"
    private val port = 2003
    private val prefix = "prefix"
    private val dataSender: DataSender = mock()
    private val basicGraphiteClient = BasicGraphiteClient(host, port, prefix, dataSender)

    @Test
    fun `sending a single metric should invoke sendData once`() {
        basicGraphiteClient.send(
            listOf(
                GraphiteMetric("metric", "0")
            )
        )
        verify(dataSender).sendData(eq(host), eq(port), any())
    }

    @Test
    fun `sending multiple metrics should invoke sendData once`() {
        basicGraphiteClient.send(
            listOf(
                GraphiteMetric("metric", "0"),
                GraphiteMetric("second.metric", "2")
            )
        )
        verify(dataSender).sendData(eq(host), eq(port), any())
    }

    @Test
    fun `sending an empty metrics list should not invoke sendData`() {
        basicGraphiteClient.send(listOf())
        verify(dataSender, never()).sendData(any(), any(), any())
    }
}
