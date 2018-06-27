package com.malinskiy.marathon.execution.strategy.impl.batching

import com.malinskiy.marathon.execution.strategy.BatchingStrategy
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import java.util.*

class FixedSizeBatchingStrategy(private val size: Int) : BatchingStrategy {
    override fun process(queue: Queue<Test>): TestBatch {
        var counter = 0
        val result = mutableListOf<Test>()
        while (counter < size && queue.isNotEmpty()) {
            counter++
            result.add(queue.poll())
        }
        return TestBatch(result)
    }
}
