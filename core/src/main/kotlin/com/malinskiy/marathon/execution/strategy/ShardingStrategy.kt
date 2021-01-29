package com.malinskiy.marathon.execution.strategy

import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.test.Test

sealed class ShardingStrategy {
    abstract fun createShard(tests: Collection<Test>): TestShard
}


data class CountShardingStrategy(private val count: Int) : ShardingStrategy() {
    override fun createShard(tests: Collection<Test>): TestShard {
        return TestShard(tests.flatMap { test ->
            (0 until count).map { test }
        })
    }
}

/**
 * Implements default sharding which executes all tests in parallel on all available devices in the pool
 */
class ParallelShardingStrategy : ShardingStrategy() {
    override fun createShard(tests: Collection<Test>): TestShard {
        return TestShard(tests)
    }

    override fun hashCode() = javaClass.canonicalName.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        val javaClass: Class<Any> = other.javaClass
        return this.javaClass.canonicalName == javaClass.canonicalName
    }

    override fun toString(): String {
        return "ParallelShardingStrategy()"
    }


}
