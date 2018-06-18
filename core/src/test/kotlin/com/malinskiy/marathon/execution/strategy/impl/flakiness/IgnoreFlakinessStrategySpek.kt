package com.malinskiy.marathon.execution.strategy.impl.flakiness

import com.malinskiy.marathon.MetricsProviderStub
import com.malinskiy.marathon.TestGenerator
import com.malinskiy.marathon.execution.TestShard
import org.amshove.kluent.shouldBe
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class IgnoreFlakinessStrategySpek : Spek({
    describe("ignore flakiness strategy test") {
        it("should return same test shard") {
            val tests = TestGenerator().create(10)
            val shard = TestShard(tests)
            val strategy = IgnoreFlakinessStrategy()
            val metricsProvider = MetricsProviderStub()
            strategy.process(shard, metricsProvider) shouldBe shard
        }
    }
})