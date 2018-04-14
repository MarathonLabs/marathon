package com.malinskiy.marathon.execution.strategy

import com.malinskiy.marathon.device.DevicePool
import com.malinskiy.marathon.execution.ExecutionShard
import com.malinskiy.marathon.execution.TestBatchResults

interface RetryStrategy {
//    var totalAllowedRetryQuota: Int?
//    var retryPerTestCaseQuota: Int?
//
//    fun shouldRetry(test: Test)
//    fun deviceSelector()
    fun process(testBatchResults: TestBatchResults, testShard: ExecutionShard, devicePool: DevicePool)
}