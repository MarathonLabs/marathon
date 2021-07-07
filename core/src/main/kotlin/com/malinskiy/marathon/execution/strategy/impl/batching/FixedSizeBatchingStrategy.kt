package com.malinskiy.marathon.execution.strategy.impl.batching

import com.malinskiy.marathon.analytics.external.Analytics
import com.malinskiy.marathon.execution.bundle.TestBundle
import com.malinskiy.marathon.execution.bundle.TestBundleIdentifier
import com.malinskiy.marathon.execution.strategy.BatchingStrategy
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import java.time.Instant
import java.util.Queue

class FixedSizeBatchingStrategy(
    private val size: Int,
    private val durationMillis: Long? = null,
    private val percentile: Double? = null,
    private val timeLimit: Instant? = null,
    private val lastMileLength: Int = 0
) : BatchingStrategy {

    override fun process(queue: Queue<Test>, analytics: Analytics, testBundleIdentifier: TestBundleIdentifier?): TestBatch {
        if (queue.size < lastMileLength && queue.isNotEmpty()) {
            //We optimize last mile by disabling batching completely.
            // This allows us to parallelize the test runs at the end instead of running batches in series
            return TestBatch(listOf(queue.poll()))
        }

        var counter = 0
        var expectedBatchDuration = 0.0
        val unbatchableTests = mutableListOf<Test>()
        val result = mutableSetOf<Test>()
        var testBundle: TestBundle? = null
        
        while (counter < size && queue.isNotEmpty()) {
            counter++
            val item = queue.poll()
            if (result.contains(item)) {
                unbatchableTests.add(item)
            } else if(testBundle != null && testBundleIdentifier?.identify(item) != testBundle) {
                unbatchableTests.add(item)
            } else {
                result.add(item)
            }

            if (durationMillis != null && percentile != null && timeLimit != null) {
                //Check for expected batch duration. If we hit the duration limit - break
                //Important part is to add at least one test so that if one test is longer than a batch
                //We still have at least one test
                val expectedTestDuration = analytics.metricsProvider.executionTime(item, percentile, timeLimit)
                expectedBatchDuration += expectedTestDuration
                if (expectedBatchDuration >= durationMillis) break
            }
        }
        if (unbatchableTests.isNotEmpty()) {
            queue.addAll(unbatchableTests)
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

    override fun toString(): String {
        return "FixedSizeBatchingStrategy(size=$size, durationMillis=$durationMillis, percentile=$percentile, timeLimit=$timeLimit, lastMileLength=$lastMileLength)"
    }


}
