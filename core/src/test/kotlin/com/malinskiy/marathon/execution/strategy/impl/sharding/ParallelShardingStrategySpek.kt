package com.malinskiy.marathon.execution.strategy.impl.sharding

import com.malinskiy.marathon.generateTests
import org.amshove.kluent.shouldBe
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class ParallelShardingStrategySpek : Spek({
    describe("parallel sharding strategy tests") {
        it("should save list of tests to TestShard w/o modifications") {
            val strategy = ParallelShardingStrategy()
            val tests = generateTests(100)
            val shard = strategy.createShard(tests)
            shard.tests.size shouldBe tests.size
            shard.tests shouldBe tests
            shard.flakyTests.size shouldBe 0
        }
    }
})
