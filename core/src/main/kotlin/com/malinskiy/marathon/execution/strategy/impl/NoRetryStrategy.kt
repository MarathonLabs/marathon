package com.malinskiy.marathon.execution.strategy.impl

import com.malinskiy.marathon.device.DevicePool
import com.malinskiy.marathon.execution.ExecutionShard
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.strategy.RetryStrategy

class NoRetryStrategy : RetryStrategy {
    override fun process(testBatchResults: TestBatchResults, testShard: ExecutionShard, devicePool: DevicePool) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}