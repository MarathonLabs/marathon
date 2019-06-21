package com.malinskiy.marathon.execution.strategy.impl.batching

import com.malinskiy.marathon.analytics.metrics.MetricsProvider
import com.malinskiy.marathon.execution.strategy.BatchingStrategy
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import java.util.*

class IsolateBatchingStrategy : BatchingStrategy {
    override fun process(queue: Queue<Test>, metricsProvider: MetricsProvider): TestBatch = TestBatch(listOf(queue.poll()))

    override fun hashCode() = javaClass.canonicalName.hashCode()

    override fun equals(other: Any?): Boolean {
        if(other == null) return false
        val javaClass: Class<Any> = other.javaClass
        return this.javaClass.canonicalName == javaClass.canonicalName
    }

    override fun toString(): String {
        return "IsolateBatchingStrategy()"
    }


}
