package com.malinskiy.marathon.execution.strategy.impl.batching

import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.strategy.BatchingStrategy
import com.malinskiy.marathon.test.TestBatch

class IsolateBatchingStrategy : BatchingStrategy {
    override fun process(testShards: TestShard): Collection<TestBatch> =
            testShards.tests.map { TestBatch(listOf(it)) }
}
