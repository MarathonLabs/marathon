package com.malinskiy.marathon.execution.strategy.impl.sharding

import com.malinskiy.marathon.generateTests
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test

class ParallelShardingStrategyTest {

    @Test
    fun `parallel sharding strategy tests should save list of tests to TestShard without modifications`() {
        val strategy = ParallelShardingStrategy()
        val tests = generateTests(100)
        val shard = strategy.createShard(tests)
        shard.tests.size shouldBe tests.size
        shard.tests shouldBe tests
        shard.flakyTests.size shouldBe 0
    }
}
