package com.malinskiy.marathon.analytics.metrics

import com.malinskiy.marathon.test.Test
import org.amshove.kluent.`should be equal to`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class NoOpMetricsProviderSpek : Spek({
    describe("test NoOpMetricsProvider") {
        it("should return 0.0 as successRate") {
            val test = Test("pkg", "clazz", "method", emptyList())
            NoOpMetricsProvider().successRate(test) `should be equal to` 0.0
        }
        it("should return 0.0 as executionTime") {
            val test = Test("pkg", "clazz", "method", emptyList())
            NoOpMetricsProvider().executionTime(test) `should be equal to` 0.0
        }
    }
})
