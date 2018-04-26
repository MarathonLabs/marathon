package com.malinskiy.marathon.execution.strategy.impl.batching

import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.strategy.BatchingStrategy
import com.malinskiy.marathon.test.TestBatch

class IsolateBatchingStrategy : BatchingStrategy {
    override fun process(testShards: Collection<TestShard>): Collection<TestBatch> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}