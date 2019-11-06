package com.malinskiy.marathon.execution.strategy.impl.flakiness

import com.malinskiy.marathon.MetricsProviderStub
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.generateTests
import org.amshove.kluent.shouldBe
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.time.Instant

class ProbabilityBasedFlakinessStrategySpek : Spek(
    {
        describe("probability-based-strategy test") {
            val instant = Instant.now()
            context("strategy with min success rate 0.8") {
                val strategy = ProbabilityBasedFlakinessStrategy(0.8, 5, instant)
                group("single test shard") {
                    val testShard = TestShard(generateTests(1))
                    it("should return 2 flaky tests for one with success rate = 0.5") {
                        val metricsProvider = MetricsProviderStub(successRate = 0.5)
                        val result = strategy.process(testShard, metricsProvider)
                        result.tests.size shouldBe 1
                        result.flakyTests.size shouldBe 2
                    }
                    it("should return zero flaky tests for one test with success rate 0.8") {
                        val metricsProvider = MetricsProviderStub(successRate = 0.8)
                        val result = strategy.process(testShard, metricsProvider)
                        result.tests.size shouldBe 1
                        result.flakyTests.size shouldBe 0
                    }
                    it("should return zero flaky tests for one test with success rate 1") {
                        val metricsProvider = MetricsProviderStub(successRate = 1.0)
                        val result = strategy.process(testShard, metricsProvider)
                        result.tests.size shouldBe 1
                        result.flakyTests.size shouldBe 0
                    }
                    it("should return one flaky test for one test with success rate = 0.7") {
                        val metricsProvider = MetricsProviderStub(successRate = 0.7)
                        val result = strategy.process(testShard, metricsProvider)
                        result.tests.size shouldBe 1
                        result.flakyTests.size shouldBe 1
                    }
                    it("should return 3 flaky tests if maxCount = 3 and success rate = 0.01") {
                        val metricsProvider = MetricsProviderStub(successRate = 0.001)
                        val result = strategy.process(testShard, metricsProvider)
                        result.tests.size shouldBe 1
                        result.flakyTests.size shouldBe 5
                    }
                }
                it("should return three flaky tests for three tests with success rate = 0.7") {
                    val metricsProvider = MetricsProviderStub(successRate = 0.7)
                    val testShard = TestShard(generateTests(3))
                    val result = strategy.process(testShard, metricsProvider)
                    result.tests.size shouldBe 3
                    result.flakyTests.size shouldBe 3
                }
            }
        }
    })
