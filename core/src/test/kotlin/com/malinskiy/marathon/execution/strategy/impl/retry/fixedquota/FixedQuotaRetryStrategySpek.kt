package com.malinskiy.marathon.execution.strategy.impl.retry.fixedquota

import com.malinskiy.marathon.TestGenerator
import com.malinskiy.marathon.TestResultsGenerator
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestShard
import org.amshove.kluent.shouldBe
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class FixedQuotaRetryStrategySpek : Spek({
    describe("fixed quota retry strategy tests") {
        group("total quota tests") {
            it("total quota is 1") {
                val strategy = FixedQuotaRetryStrategy(totalAllowedRetryQuota = 1)
                val poolId = DevicePoolId("DevicePoolId-1")
                val tests = TestGenerator().create(10)
                val testResults = TestResultsGenerator().create(tests)
                strategy.process(poolId, testResults, TestShard(tests)).size shouldBe 1
            }
            it("total quota more than size of the input list") {
                val strategy = FixedQuotaRetryStrategy(totalAllowedRetryQuota = 10 + 1)
                val poolId = DevicePoolId("DevicePoolId-1")
                val tests = TestGenerator().create(10)
                val testResults = TestResultsGenerator().create(tests)
                strategy.process(poolId, testResults, TestShard(tests)).size shouldBe 10
            }
        }
        group("flakiness tests") {
            val strategy by memoized { FixedQuotaRetryStrategy() }
            val poolId = DevicePoolId("DevicePoolId-1")
            val tests = TestGenerator().create(50)
            val testResults = TestResultsGenerator().create(tests)
            it("should return all tests if flakytests size = 0") {
                strategy.process(poolId, testResults, TestShard(tests)).size shouldBe 50
            }
            it("should return 0 tests if flakiness strategy added 3 flaky tests per test") {
                strategy.process(poolId, testResults, TestShard(tests, flakyTests = tests + tests + tests)).size shouldBe 0
            }
        }
    }
})
