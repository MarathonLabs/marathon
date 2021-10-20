package com.malinskiy.marathon.execution.strategy.impl.sharding

import com.malinskiy.marathon.config.strategy.ShardingStrategyConfiguration
import com.malinskiy.marathon.extension.toShardingStrategy
import com.malinskiy.marathon.generateTests
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test

class CountShardingStrategyTest {

    @Test
    fun `"N-count sharding strategy should return 5 tests for list of 1 test and n = 5`() {
        val strategy = ShardingStrategyConfiguration.CountShardingStrategyConfiguration(5).toShardingStrategy()
        val tests = generateTests(1)
        val result = strategy.createShard(tests)
        result.tests.size shouldBe 5
        result.flakyTests.size shouldBe 0
    }
}
