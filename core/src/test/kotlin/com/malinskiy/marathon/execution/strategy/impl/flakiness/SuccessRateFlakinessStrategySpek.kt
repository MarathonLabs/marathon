package com.malinskiy.marathon.execution.strategy.impl.flakiness

import com.malinskiy.marathon.MetricsProviderStub
import com.malinskiy.marathon.TestGenerator
import com.malinskiy.marathon.execution.TestShard
import org.amshove.kluent.shouldBe
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class SuccessRateFlakinessStrategySpek : Spek({
    describe("probability-based-strategy test") {
        group("min success rate 0.8") {
            it("should return 3 tests instead of 1 if success rate = 0.5") {
                val metricsProvider = MetricsProviderStub()
                val strategy = SuccessRateFlakinessStrategy(0.8)
                val testShard = TestShard(TestGenerator().create(1))
                val result = strategy.process(testShard, metricsProvider)
                result.tests.size shouldBe 3
            }
            it("should return 2 tests instead of 1 if success rate = 0.7") {
                val metricsProvider = MetricsProviderStub(successRate = 0.7)
                val strategy = SuccessRateFlakinessStrategy(0.8)
                val testShard = TestShard(TestGenerator().create(1))
                val result = strategy.process(testShard, metricsProvider)
                result.tests.size shouldBe 2
            }

            it("should return 6 tests instead of 3 if success rate = 0.7") {
                val metricsProvider = MetricsProviderStub(successRate = 0.7)
                val strategy = SuccessRateFlakinessStrategy(0.8)
                val testShard = TestShard(TestGenerator().create(3))
                val result = strategy.process(testShard, metricsProvider)
                result.tests.size shouldBe 6
            }
        }
    }
})
