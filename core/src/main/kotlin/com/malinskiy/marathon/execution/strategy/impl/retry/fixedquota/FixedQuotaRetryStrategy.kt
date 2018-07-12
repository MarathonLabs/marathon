package com.malinskiy.marathon.execution.strategy.impl.retry.fixedquota

import com.fasterxml.jackson.annotation.JsonProperty
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.strategy.RetryStrategy
import com.malinskiy.marathon.test.Test

class FixedQuotaRetryStrategy(@JsonProperty("totalAllowedRetryQuota") totalAllowedRetryQuota: Int = 200,
                              @JsonProperty("retryPerTestQuota") retryPerTestQuota: Int = 3) : RetryStrategy {
    private val retryWatchdog = RetryWatchdog(totalAllowedRetryQuota, retryPerTestQuota)
    private val poolTestCaseFailureAccumulator = PoolTestFailureAccumulator()

    override fun process(devicePoolId: DevicePoolId, tests: Collection<Test>, testShard: TestShard): List<Test> {
        return tests.filter {
            poolTestCaseFailureAccumulator.record(devicePoolId, it)
            val flakinessResultCount = testShard.flakyTests.count { it == it }
            retryWatchdog.requestRetry(poolTestCaseFailureAccumulator.getCount(devicePoolId, it) + flakinessResultCount)
        }
    }

    override fun equals(other: Any?): Boolean {
        if(other == null) return false
        val javaClass: Class<Any> = other.javaClass
        return this.javaClass.canonicalName == javaClass.canonicalName
    }
}
