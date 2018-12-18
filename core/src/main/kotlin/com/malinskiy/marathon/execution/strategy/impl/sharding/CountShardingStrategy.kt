package com.malinskiy.marathon.execution.strategy.impl.sharding

import com.fasterxml.jackson.annotation.JsonProperty
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.strategy.ShardingStrategy
import com.malinskiy.marathon.test.Test

class CountShardingStrategy(@JsonProperty("count") private val count: Int) : ShardingStrategy {
    override fun createShard(tests: Collection<Test>): TestShard {
        return TestShard(tests.flatMap { test ->
            (0 until count).map { test }
        })
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CountShardingStrategy

        if (count != other.count) return false

        return true
    }

    override fun hashCode(): Int {
        return count
    }

    override fun toString(): String {
        return "CountShardingStrategy(count=$count)"
    }


}
