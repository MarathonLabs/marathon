package com.malinskiy.marathon.analytics.metrics.remote

import com.malinskiy.marathon.analytics.external.MetricsProvider
import com.malinskiy.marathon.analytics.external.MetricsProviderImpl
import com.malinskiy.marathon.generateTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInRange
import org.amshove.kluent.shouldEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

abstract class BaseMetricsProviderIntegrationTest {

    private lateinit var metricsProvider: MetricsProvider
    private val test = generateTest()

    protected abstract fun createRemoteDataSource(): RemoteDataSource

    @BeforeEach
    fun setUp() {
        metricsProvider = MetricsProviderImpl(createRemoteDataSource(), .0, Duration.ofMinutes(5))
    }

    @Test
    fun `on empty db success rate default value is 0`() {
        val result = metricsProvider.successRate(test, Instant.now())
        result shouldBeEqualTo 0.0
    }

    @Test
    fun `execution time default value is 300_000`() {
        val result = metricsProvider.executionTime(test, 90.0, Instant.now())
        result shouldBeEqualTo 300_000.0
    }

    @Test
    fun `verify execution time 50 percentile for last two hours`() {
        val result = metricsProvider.executionTime(test, 50.0, Instant.now().minus(2, ChronoUnit.HOURS))
        result shouldBeInRange 5000.0..6000.0
    }

    @Test
    fun `verify execution time 90 percentile for last two hours`() {
        val result = metricsProvider.executionTime(test, 90.0, Instant.now().minus(2, ChronoUnit.HOURS))
        result shouldBeInRange 9000.0..10000.0
    }

    @Test
    fun `verify execution time 50 percentile for 25 minutes`() {
        val result = metricsProvider.executionTime(test, 50.0, Instant.now().minus(25, ChronoUnit.MINUTES))
        result shouldBeEqualTo 2000.0
    }

    @Test
    fun `verify execution time 90 percentile for 25 minutes`() {
        val result = metricsProvider.executionTime(test, 90.0, Instant.now().minus(35, ChronoUnit.MINUTES))
        result shouldBeEqualTo 4000.0
    }

    @Test
    fun `verify test success rate should return 1 for last 50 minutes`() {
        val result = metricsProvider.successRate(test, Instant.now().minus(50, ChronoUnit.MINUTES))
        result shouldBeEqualTo 1.0
    }

    @Test
    fun `verify test success rate should return 0_833 for last 65 minutes`() {
        val result = metricsProvider.successRate(test, Instant.now().minus(65, ChronoUnit.MINUTES))
        result shouldBeInRange 0.833..0.834
    }

    @Test
    fun `verify test success rate should return 0_5 for last 2 hours`() {
        val result = metricsProvider.successRate(test, Instant.now().minus(2, ChronoUnit.HOURS))
        result shouldBeEqualTo 0.5
    }
}
