package com.malinskiy.marathon.analytics.metrics

import com.malinskiy.marathon.analytics.external.NoOpMetricsProvider
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import java.time.Instant
import com.malinskiy.marathon.test.Test as MarathonTest

class NoOpMetricsProviderTest {
    @Test
    fun shouldReturn0AsSuccessRate() {
        val test = MarathonTest("pkg", "clazz", "method", emptyList())
        NoOpMetricsProvider().successRate(test, Instant.now()) `should be equal to` 0.0
    }

    @Test
    fun shouldReturn0AsExecutionTime() {
        val test = MarathonTest("pkg", "clazz", "method", emptyList())
        NoOpMetricsProvider().executionTime(test, 90.0, Instant.now()) `should be equal to` 0.0
    }
}
