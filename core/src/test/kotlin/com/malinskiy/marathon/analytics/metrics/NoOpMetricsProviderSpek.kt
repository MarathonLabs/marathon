package com.malinskiy.marathon.analytics.metrics

import com.malinskiy.marathon.test.Test
import org.amshove.kluent.`should be equal to`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.time.Instant

class NoOpMetricsProviderSpek : Spek({
    describe("test NoOpMetricsProvider") {
        it("should return 0.0 as successRate") {
            val test = Test("pkg", "clazz", "method", emptyList())
            NoOpMetricsProvider().successRate(test, Instant.now()) `should be equal to` 0.0
        }
        it("should return 0.0 as executionTime") {
            val test = Test("pkg", "clazz", "method", emptyList())
            NoOpMetricsProvider().executionTime(test, 90.0, Instant.now()) `should be equal to` 0.0
        }
    }
})
