package com.malinskiy.marathon.execution.strategy.impl.retry

import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.strategy.RetryStrategy
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toSafeTestName

class FixedQuotaRetryStrategy(private val totalAllowedRetryQuota: Int = 200,
                              private val retryPerTestQuota: Int = 3) : RetryStrategy {
    val map = mutableMapOf<Test, Int>()
    var totalRetries = 0

    override fun process(tests: Collection<Test>, testShard: TestShard): List<Test> {
        val result = mutableListOf<Test>()
        for (test in tests) {
            if (totalRetries < totalAllowedRetryQuota) {
                map.putIfAbsent(test, 0)
                val flakinessResultCount = testShard.tests.count { it == test }
                println("${test.toSafeTestName()} flakiness count = $flakinessResultCount")
                if (map[test]!! + flakinessResultCount < retryPerTestQuota) {
                    map.computeIfPresent(test, { _, counter -> counter + 1 })
                    totalRetries++
                    println("total retries $totalRetries of $totalAllowedRetryQuota")
                    result.add(test)
                }
            }
        }
        return result
    }
}
