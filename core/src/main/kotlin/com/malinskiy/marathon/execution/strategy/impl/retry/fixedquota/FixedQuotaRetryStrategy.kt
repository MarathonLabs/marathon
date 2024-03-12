package com.malinskiy.marathon.execution.strategy.impl.retry.fixedquota

import com.malinskiy.marathon.config.strategy.RetryStrategyConfiguration
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.progress.PoolProgressAccumulator
import com.malinskiy.marathon.execution.queue.TestAction
import com.malinskiy.marathon.execution.strategy.RetryStrategy

class FixedQuotaRetryStrategy(private val cnf: RetryStrategyConfiguration.FixedQuotaRetryStrategyConfiguration) : RetryStrategy {
    private val retryWatchdog = RetryWatchdog(cnf.totalAllowedRetryQuota, cnf.retryPerTestQuota)
    private val poolTestCaseFailureAccumulator = PoolTestFailureAccumulator()

    override fun process(
        devicePoolId: DevicePoolId,
        tests: Collection<TestResult>,
        testShard: TestShard,
        poolProgressAccumulator: PoolProgressAccumulator
    ): List<TestResult> {
        return tests.filter { testResult ->
            poolTestCaseFailureAccumulator.record(devicePoolId, testResult.test)
            val flakinessResultCount = testShard.flakyTests.count { it == testResult.test }
            val failuresCount = poolTestCaseFailureAccumulator.getCount(devicePoolId, testResult.test) + flakinessResultCount
            if (retryWatchdog.retryPossible(failuresCount) && poolProgressAccumulator.retryTest(testResult.test) != TestAction.Complete) {
                retryWatchdog.requestRetry(failuresCount)
            } else {
                false
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FixedQuotaRetryStrategy

        if (retryWatchdog != other.retryWatchdog) return false

        return true
    }

    override fun hashCode(): Int {
        return retryWatchdog.hashCode()
    }

    override fun toString(): String {
        return "FixedQuotaRetryStrategy(retryWatchdog=$retryWatchdog, poolTestCaseFailureAccumulator=$poolTestCaseFailureAccumulator)"
    }
}
