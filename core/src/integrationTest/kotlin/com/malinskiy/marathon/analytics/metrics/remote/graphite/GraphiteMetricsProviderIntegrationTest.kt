package com.malinskiy.marathon.analytics.metrics.remote.graphite

import com.malinskiy.marathon.analytics.external.MetricsProvider
import com.malinskiy.marathon.analytics.external.MetricsProviderImpl
import com.malinskiy.marathon.analytics.external.graphite.BasicGraphiteClient
import com.malinskiy.marathon.analytics.external.graphite.GraphiteTracker
import com.malinskiy.marathon.analytics.internal.sub.TestEvent
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.DeviceStub
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.generateTest
import org.amshove.kluent.shouldBeInRange
import org.amshove.kluent.shouldEqualTo
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

class GraphiteMetricsProviderIntegrationTest {

    private lateinit var metricsProvider: MetricsProvider

    @BeforeEach
    fun setUp() {
        metricsProvider =
            MetricsProviderImpl(GraphiteDataSource(QueryableGraphiteClient(host, container.getMappedPort(httpPort)), prefix))
    }

    @Test
    fun `on empty db success rate default value is 0`() {
        val result = metricsProvider.successRate(test, Instant.now())
        result shouldEqualTo 0.0
    }

    @Test
    fun `execution time default value is 300_000`() {
        val result = metricsProvider.executionTime(test, 90.0, Instant.now())
        result shouldEqualTo 300_000.0
    }

    @Test
    fun `verify execution time 50 percentile for last two hours`() {
        val result = metricsProvider.executionTime(test, 50.0, Instant.now().minus(3, ChronoUnit.HOURS))
        result shouldBeInRange 5000.0..6000.0
    }

    @Test
    fun `verify execution time 90 percentile for last two hours`() {
        val result = metricsProvider.executionTime(test, 90.0, Instant.now().minus(3, ChronoUnit.HOURS))
        result shouldBeInRange 9000.0..10000.0
    }

    @Test
    fun `verify execution time 50 percentile for 25 minutes`() {
        val result = metricsProvider.executionTime(
            test,
            50.0,
            Instant.now().minus(25, ChronoUnit.MINUTES)
        )
        result shouldEqualTo 2000.0
    }

    @Test
    fun `verify execution time 90 percentile for 25 minutes`() {
        val result = metricsProvider.executionTime(
            test,
            90.0,
            Instant.now().minus(35, ChronoUnit.MINUTES)
        )
        result shouldEqualTo 4000.0
    }

    @Test
    fun `verify test success rate should return 1 for last 50 minutes`() {
        val result = metricsProvider.successRate(test, Instant.now().minus(50, ChronoUnit.MINUTES))
        result shouldEqualTo 1.0
    }

    @Test
    fun `verify test success rate should return 0_833 for last 65 minutes`() {
        val result = metricsProvider.successRate(test, Instant.now().minus(65, ChronoUnit.MINUTES))
        result.shouldBeInRange(0.833, 0.834)
    }

    @Test
    fun `verify test success rate should return 0_5 for last 2 hours`() {
        val result = metricsProvider.successRate(test, Instant.now().minus(2, ChronoUnit.HOURS))
        result shouldEqualTo 0.5
    }

    private companion object {
        const val host = "localhost"
        const val httpPort = 80
        const val port = 2003
        const val prefix = "prefix"

        val container = GenericContainer<Nothing>("graphiteapp/graphite-statsd:1.1.7-6").apply {
            withExposedPorts(httpPort, port)
            waitingFor(HostPortWaitStrategy())
            withStartupTimeout(Duration.ofSeconds(30))
        }

        val test = generateTest()

        @BeforeAll
        @JvmStatic
        fun `start container and prepare data`() {
            container.start()
            val graphiteClient = BasicGraphiteClient(host, container.getMappedPort(port), prefix)
            val graphiteTracker = GraphiteTracker(graphiteClient)
            prepareData(graphiteTracker)
        }

        @AfterAll
        @JvmStatic
        fun `stop container`() {
            container.stop()
        }

        private fun prepareData(graphiteTracker: GraphiteTracker) {
            val instant = Instant.now()
            val deviceInfo = DeviceStub().toDeviceInfo()
            val events = listOf(
                testPassed(
                    device = deviceInfo,
                    duration = 1_000,
                    whenWasSent = instant.minus(1, ChronoUnit.MINUTES)
                ),
                testPassed(
                    device = deviceInfo,
                    duration = 2_000,
                    whenWasSent = instant.minus(10, ChronoUnit.MINUTES)
                ),
                testPassed(
                    device = deviceInfo,
                    duration = 3_000,
                    whenWasSent = instant.minus(20, ChronoUnit.MINUTES)
                ),
                testPassed(
                    device = deviceInfo,
                    duration = 4_000,
                    whenWasSent = instant.minus(30, ChronoUnit.MINUTES)
                ),
                testPassed(
                    device = deviceInfo,
                    duration = 5_000,
                    whenWasSent = instant.minus(40, ChronoUnit.MINUTES)
                ),
                testFailed(
                    device = deviceInfo,
                    duration = 6_000,
                    whenWasSent = instant.minus(60, ChronoUnit.MINUTES)
                ),
                testFailed(
                    device = deviceInfo,
                    duration = 7_000,
                    whenWasSent = instant.minus(70, ChronoUnit.MINUTES)
                ),
                testFailed(
                    device = deviceInfo,
                    duration = 8_000,
                    whenWasSent = instant.minus(80, ChronoUnit.MINUTES)
                ),
                testFailed(
                    device = deviceInfo,
                    duration = 9_000,
                    whenWasSent = instant.minus(90, ChronoUnit.MINUTES)
                ),
                testFailed(
                    device = deviceInfo,
                    duration = 10_000,
                    whenWasSent = instant.minus(100, ChronoUnit.MINUTES)
                )
            )
            events.forEach {
                graphiteTracker.track(it)
            }
            Thread.sleep(5_000)
        }

        private fun testPassed(device: DeviceInfo, duration: Long, whenWasSent: Instant) = TestEvent(
            whenWasSent,
            DevicePoolId("omni"),
            device,
            TestResult(
                test = test,
                device = device,
                status = TestStatus.PASSED,
                startTime = whenWasSent.minusMillis(duration).toEpochMilli(),
                endTime = whenWasSent.toEpochMilli()
            ),
            true
        )

        private fun testFailed(device: DeviceInfo, duration: Long, whenWasSent: Instant) = TestEvent(
            whenWasSent,
            DevicePoolId("omni"),
            device,
            TestResult(
                test = test,
                device = device,
                status = TestStatus.FAILURE,
                startTime = whenWasSent.minusMillis(duration).toEpochMilli(),
                endTime = whenWasSent.toEpochMilli()
            ),
            true
        )
    }
}
