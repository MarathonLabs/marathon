package com.malinskiy.marathon.execution.strategy

import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.test.TestBatch

interface BatchingStrategy {
    fun process(testShards: TestShard): Collection<TestBatch>
}
