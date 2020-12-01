package com.malinskiy.marathon.execution.strategy.impl.retry.fixedquota

import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.generateTestResults
import com.malinskiy.marathon.generateTests
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test

class FixedQuotaRetryStrategyTest {

    @Test
    fun `total quota tests, total quota is 1`() {
        val strategy = FixedQuotaRetryStrategy(totalAllowedRetryQuota = 1)
        val poolId = DevicePoolId("DevicePoolId-1")
        val tests = generateTests(10)
        val testResults = generateTestResults(tests)
        strategy.process(poolId, testResults, TestShard(tests)).size shouldBe 1
    }

    @Test
    fun `total quota tests, total quota more than size of the input list`() {
        val strategy = FixedQuotaRetryStrategy(totalAllowedRetryQuota = 10 + 1)
        val poolId = DevicePoolId("DevicePoolId-1")
        val tests = generateTests(10)
        val testResults = generateTestResults(tests)
        strategy.process(poolId, testResults, TestShard(tests)).size shouldBe 10
    }

    @Test
    fun `flakiness tests, should return all tests if flakytests size = 0`() {
        val strategy = FixedQuotaRetryStrategy()
        val poolId = DevicePoolId("DevicePoolId-1")
        val tests = generateTests(50)
        val testResults = generateTestResults(tests)

        strategy.process(poolId, testResults, TestShard(tests)).size shouldBe 50
    }

    @Test
    fun `flakiness tests, should return 0 tests if flakiness strategy added 3 flaky tests per test`() {
        val strategy = FixedQuotaRetryStrategy()
        val poolId = DevicePoolId("DevicePoolId-1")
        val tests = generateTests(50)
        val testResults = generateTestResults(tests)

        strategy.process(
            poolId,
            testResults,
            TestShard(tests, flakyTests = tests + tests + tests)
        ).size shouldBe 0
    }
}
