package com.malinskiy.marathon.execution.strategy.impl.batching

import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.strategy.BatchingStrategy
import com.malinskiy.marathon.test.TestBatch

class FixedSizeBatchingStrategy(private val size: Int) : BatchingStrategy {
    override fun process(testShards: Collection<TestShard>): Collection<TestBatch> {
        return testShards.flatMap { it.tests.chunked(size).map { TestBatch(it) } }
    }
}
