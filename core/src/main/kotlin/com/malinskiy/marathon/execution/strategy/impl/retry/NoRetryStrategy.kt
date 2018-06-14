package com.malinskiy.marathon.execution.strategy.impl.retry

import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.strategy.RetryStrategy
import com.malinskiy.marathon.test.Test

class NoRetryStrategy : RetryStrategy {
    override fun process(tests: Collection<Test>, testShard: TestShard) : List<Test>{
        return emptyList()
    }
}
