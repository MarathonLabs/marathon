package com.malinskiy.marathon.analytics.metrics.remote.graphite

import com.malinskiy.marathon.analytics.external.graphite.BasicGraphiteClient
import com.malinskiy.marathon.analytics.external.graphite.GraphiteTracker
import com.malinskiy.marathon.analytics.metrics.remote.BaseMetricsProviderIntegrationTest
import com.malinskiy.marathon.analytics.metrics.remote.getTestEvents
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy
import java.time.Duration

class GraphiteMetricsProviderIntegrationTest : BaseMetricsProviderIntegrationTest() {

    override fun createRemoteDataSource() = GraphiteDataSource(QueryableGraphiteClient(host, container.getMappedPort(httpPort)), prefix)

    private companion object {
        const val host = "localhost"
        const val httpPort = 80
        const val port = 2003
        const val prefix = "prefix"

        val container: GraphiteContainer = GraphiteContainer()
            .withExposedPorts(httpPort, port)
            .waitingFor(HostPortWaitStrategy())
            .withStartupTimeout(Duration.ofSeconds(30))

        @BeforeAll
        @JvmStatic
        fun `start container and prepare data`() {
            container.start()
            val tracker = GraphiteTracker(BasicGraphiteClient(host, container.getMappedPort(port), prefix), readOnly = false)
            getTestEvents().forEach { tracker.track(it) }
            // Graphite needs a couple of seconds to process the data. There's no way to wait for it in a more intelligent manner.
            Thread.sleep(5_000)
        }

        @AfterAll
        @JvmStatic
        fun `stop container`() {
            container.stop()
        }
    }
}
