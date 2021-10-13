package com.malinskiy.marathon.execution.strategy.impl.sharding

import com.malinskiy.marathon.config.strategy.ShardingStrategyConfiguration
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.strategy.ShardingStrategy
import com.malinskiy.marathon.test.Test

class CountShardingStrategy(private val cnf: ShardingStrategyConfiguration.CountShardingStrategyConfiguration) : ShardingStrategy {
    override fun createShard(tests: Collection<Test>): TestShard {
        return TestShard(tests.flatMap { test ->
            (0 until cnf.count).map { test }
        })
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CountShardingStrategy

        if (cnf.count != cnf.count) return false

        return true
    }

    override fun hashCode(): Int {
        return cnf.count
    }

    override fun toString(): String {
        return "CountShardingStrategy(count=${cnf.count})"
    }
}
