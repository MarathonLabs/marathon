package com.malinskiy.marathon.execution.strategy.impl.flakiness

import com.malinskiy.marathon.MetricsProviderStub
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.generateTests
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class ProbabilityBasedFlakinessStrategyTest {
    val instant: Instant = Instant.now()
    val strategy = ProbabilityBasedFlakinessStrategy(0.8, 5, instant)

    @Test
    fun `strategy with min success rate 0_8 should return 2 flaky tests for one with success rate = 0_5 `() {
        val testShard = TestShard(generateTests(1))
        val metricsProvider = MetricsProviderStub(successRate = 0.5)
        val result = strategy.process(testShard, metricsProvider)
        result.tests.size shouldBe 1
        result.flakyTests.size shouldBe 2
    }

    @Test
    fun `strategy with min success rate 0_8 should return zero flaky tests for one test with success rate 0_8`() {
        val testShard = TestShard(generateTests(1))
        val metricsProvider = MetricsProviderStub(successRate = 0.8)
        val result = strategy.process(testShard, metricsProvider)
        result.tests.size shouldBe 1
        result.flakyTests.size shouldBe 0
    }

    @Test
    fun `strategy with min success rate 0_8 should return zero flaky tests for one test with success rate 1`() {
        val testShard = TestShard(generateTests(1))
        val metricsProvider = MetricsProviderStub(successRate = 1.0)
        val result = strategy.process(testShard, metricsProvider)
        result.tests.size shouldBe 1
        result.flakyTests.size shouldBe 0
    }

    @Test
    fun `strategy with min success rate 0_8 should return one flaky test for one test with success rate = 0_7`() {
        val testShard = TestShard(generateTests(1))
        val metricsProvider = MetricsProviderStub(successRate = 0.7)
        val result = strategy.process(testShard, metricsProvider)
        result.tests.size shouldBe 1
        result.flakyTests.size shouldBe 1
    }

    @Test
    fun `strategy with min success rate 0_8 should return 3 flaky tests if maxCount = 3 and success rate = 0_01`() {
        val testShard = TestShard(generateTests(1))
        val metricsProvider = MetricsProviderStub(successRate = 0.001)
        val result = strategy.process(testShard, metricsProvider)
        result.tests.size shouldBe 1
        result.flakyTests.size shouldBe 5
    }

    @Test
    fun `strategy with min success rate 0_8 should return three flaky tests for three tests in a shard with success rate = 0_7`() {
        val testShard = TestShard(generateTests(3))
        val metricsProvider = MetricsProviderStub(successRate = 0.7)
        val result = strategy.process(testShard, metricsProvider)
        result.tests.size shouldBe 3
        result.flakyTests.size shouldBe 3
    }
}
