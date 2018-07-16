package com.malinskiy.marathon.execution.strategy.impl.batching

import com.fasterxml.jackson.annotation.JsonProperty
import com.malinskiy.marathon.execution.strategy.BatchingStrategy
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import java.util.*

class FixedSizeBatchingStrategy(@JsonProperty("size") private val size: Int) : BatchingStrategy {
    override fun process(queue: Queue<Test>): TestBatch {
        var counter = 0
        val duplicates = mutableListOf<Test>()
        val result = mutableSetOf<Test>()
        while (counter < size && queue.isNotEmpty()) {
            counter++
            val item = queue.poll()
            if (result.contains(item)) {
                duplicates.add(item)
            } else {
                result.add(item)
            }
        }
        if (duplicates.isNotEmpty()) {
            queue.addAll(duplicates)
        }
        return TestBatch(result.toList())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FixedSizeBatchingStrategy

        if (size != other.size) return false

        return true
    }

    override fun hashCode(): Int {
        return size
    }


}
