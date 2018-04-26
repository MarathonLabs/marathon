package com.malinskiy.marathon.execution.strategy.impl.retry

import com.malinskiy.marathon.device.DevicePool
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.strategy.RetryStrategy

class NoRetryStrategy : RetryStrategy {
    override fun process(testBatchResults: TestBatchResults, testShard: TestShard, devicePool: DevicePool) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}