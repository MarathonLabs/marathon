package com.malinskiy.marathon.execution.strategy

import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.test.Test

interface ShardingStrategy {
    fun createShards(tests: Collection<Test>): Collection<TestShard>
}
