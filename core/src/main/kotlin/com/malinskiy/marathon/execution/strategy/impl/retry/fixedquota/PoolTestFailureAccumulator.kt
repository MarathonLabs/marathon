package com.malinskiy.marathon.execution.strategy.impl.retry.fixedquota

import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.test.Test
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class PoolTestFailureAccumulator {
    private data class TestRetryCounter(val test: Test, val counter: AtomicInteger) {
        override fun hashCode(): Int {
            return test.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as TestRetryCounter

            if (test != other.test) return false

            return true
        }
    }

    private val map = ConcurrentHashMap<DevicePoolId, MutableSet<TestRetryCounter>>()

    fun record(pool: DevicePoolId, test: Test) {
        map.computeIfAbsent(pool) { _ -> HashSet() }

        val testRetryCounter = (map[pool] ?: mutableSetOf()).find { it.test == test }
        if (testRetryCounter != null) {
            testRetryCounter.counter.incrementAndGet()
        } else {
            val retryCounter = createNew(test).apply {
                counter.incrementAndGet()
            }
            map[pool]?.add(retryCounter)
        }
    }

    fun getCount(pool: DevicePoolId, test: Test): Int =
            map[pool]?.find { it.test == test }?.counter?.get() ?: 0

    private fun createNew(test: Test): TestRetryCounter {
        return TestRetryCounter(test, AtomicInteger(0))
    }
}


