package com.malinskiy.marathon.execution.strategy.impl.batching

import com.malinskiy.marathon.TestGenerator
import com.malinskiy.marathon.execution.TestShard
import org.amshove.kluent.shouldBe
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class FixedSizeBatchingStrategySpek : Spek({
    describe("test batching strategy with fixed size") {
        it("should create 5 batches for 50 tests with batch size 10") {
            val tests = TestGenerator().create(50)
            val shard = TestShard(tests)
            val strategy = FixedSizeBatchingStrategy(10)
            val batches = strategy.process(listOf(shard))
            batches.size shouldBe 5
        }
        it("should create 1 batch for 10 tests with batch size 10"){
            val tests = TestGenerator().create(10)
            val shard = TestShard(tests)
            val strategy = FixedSizeBatchingStrategy(10)
            val batches = strategy.process(listOf(shard))
            batches.size shouldBe 1
        }
    }
})
