package com.malinskiy.marathon.execution.strategy.impl.flakiness

import com.malinskiy.marathon.MetricsProviderStub
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.generateTests
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test

class IgnoreFlakinessStrategyTest {
    @Test
    fun `ignore flakiness strategy test should return same test shard`() {
        val tests = generateTests(10)
        val shard = TestShard(tests)
        val strategy = IgnoreFlakinessStrategy()
        val metricsProvider = MetricsProviderStub()
        strategy.process(shard, metricsProvider) shouldBe shard
    }
}
