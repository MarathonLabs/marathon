package com.malinskiy.marathon.execution.strategy.impl.sharding

import com.malinskiy.marathon.generateTests
import org.amshove.kluent.shouldBe
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class CountShardingStrategySpek : Spek({
    describe("N-count sharding strategy") {
        it("should return 5 tests for list of 1 test and n = 5") {
            val strategy = CountShardingStrategy(5)
            val tests = generateTests(1)
            val result = strategy.createShard(tests)
            result.tests.size shouldBe 5
            result.flakyTests.size shouldBe 0
        }
    }
})
