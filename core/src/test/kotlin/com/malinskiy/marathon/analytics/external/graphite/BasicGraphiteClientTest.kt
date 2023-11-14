/*
 * This file is a copy of:
 * https://github.com/mihkels/graphite-client-kotlin/blob/master/graphite-client/src/test/kotlin/com/mihkels/graphite/client/BasicGraphiteClientTest.kt
 * with some minor modifications.
 * It can be removed when graphite-kotlin-client gets published to Maven Central.
 */

package com.malinskiy.marathon.analytics.external.graphite

import org.mockito.kotlin.mock
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

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
