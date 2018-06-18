package com.malinskiy.marathon.execution.strategy.impl.retry.fixedquota

import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.strategy.RetryStrategy
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toSafeTestName

class FixedQuotaRetryStrategy(totalAllowedRetryQuota: Int = 200,
                              retryPerTestQuota: Int = 3) : RetryStrategy {
    private val retryWatchdog = RetryWatchdog(totalAllowedRetryQuota, retryPerTestQuota)
    private val poolTestCaseFailureAccumulator = PoolTestCaseFailureAccumulator()

    override fun process(devicePoolId: DevicePoolId, tests: Collection<Test>, testShard: TestShard): List<Test> {
        return tests.filter {
            poolTestCaseFailureAccumulator.record(devicePoolId, it)
            val flakinessResultCount = testShard.flakyTests.count { it == it }
            retryWatchdog.requestRetry(poolTestCaseFailureAccumulator.getCount(devicePoolId, it) + flakinessResultCount)
        }
    }
}