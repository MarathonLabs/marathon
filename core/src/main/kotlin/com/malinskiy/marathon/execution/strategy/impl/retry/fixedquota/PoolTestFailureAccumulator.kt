package com.malinskiy.marathon.execution.strategy.impl.retry.fixedquota

import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.test.Test
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class PoolTestCaseFailureAccumulator {
    private data class TestRetryCounter(val test: Test, val counter: AtomicInteger) {
        override fun equals(other: Any?): Boolean {
            return if (other is TestRetryCounter) {
                other.test == test
            } else {
                false
            }
        }

        override fun hashCode(): Int {
            return test.hashCode()
        }
    }

    private val map = ConcurrentHashMap<DevicePoolId, MutableSet<TestRetryCounter>>()

    fun record(pool: DevicePoolId, test: Test) {
        map.computeIfAbsent(pool) { _ -> HashSet<TestRetryCounter>().apply { add(createNew(test)) } }

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

    fun getCount(pool: DevicePoolId, testCaseEvent: Test): Int =
            map[pool]?.find { it.test == testCaseEvent }?.counter?.get() ?: 0

    private fun createNew(test: Test): TestRetryCounter {
        return TestRetryCounter(test, AtomicInteger(0))
    }
}


