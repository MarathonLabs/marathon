package com.malinskiy.marathon.execution.strategy.impl.retry.fixedquota

import com.malinskiy.marathon.generateTests
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.generateTest
import com.malinskiy.marathon.generateTestResults
import org.amshove.kluent.shouldBe
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

object FixedQuotaRetryStrategySpek : Spek({
    describe("fixed quota retry strategy tests") {
        group("no retry test matchers") {
            val poolId = DevicePoolId("DevicePoolId-1")
            it("with sufficient quota") {
                val tests = generateTests(9)
                val testResults = generateTestResults(tests)
                val noRetryTestMatchers = listOf(tests[0].toTestMatcher())
                val strategy = FixedQuotaRetryStrategy(totalAllowedRetryQuota = 10, noRetryTestMatchers = noRetryTestMatchers)
                strategy.process(poolId, testResults, TestShard(tests)).size shouldBe 8
            }

            it("tests with same names") {
                val noRetryTest = generateTest(pkg = "com.test", clazz = "SomeExtraTest", method = "helloWorld")
                val toRetryTest = noRetryTest.copy(clazz = "SomeTest")
                val noRetryMatcher = TestNameRegexTestMatcher(pkg = null, clazz = "^SomeExtraTest$", method = "^helloWorld$")
                val tests = listOf(noRetryTest, toRetryTest)
                val testResults = generateTestResults(tests)
                val strategy = FixedQuotaRetryStrategy(totalAllowedRetryQuota = 10, noRetryTestMatchers = listOf(noRetryMatcher))
                strategy.process(poolId, testResults, TestShard(tests)).size shouldBe 1
            }
        }
        group("total quota tests") {
            it("total quota is 1") {
                val strategy = FixedQuotaRetryStrategy(totalAllowedRetryQuota = 1)
                val poolId = DevicePoolId("DevicePoolId-1")
                val tests = generateTests(10)
                val testResults = generateTestResults(tests)
                strategy.process(poolId, testResults, TestShard(tests)).size shouldBe 1
            }
            it("total quota more than size of the input list") {
                val strategy = FixedQuotaRetryStrategy(totalAllowedRetryQuota = 10 + 1)
                val poolId = DevicePoolId("DevicePoolId-1")
                val tests = generateTests(10)
                val testResults = generateTestResults(tests)
                strategy.process(poolId, testResults, TestShard(tests)).size shouldBe 10
            }
        }
        group("flakiness tests") {
            val strategy by memoized { FixedQuotaRetryStrategy() }
            val poolId = DevicePoolId("DevicePoolId-1")
            val tests = generateTests(50)
            val testResults = generateTestResults(tests)
            it("should return all tests if flakytests size = 0") {
                strategy.process(poolId, testResults, TestShard(tests)).size shouldBe 50
            }
            it("should return 0 tests if flakiness strategy added 3 flaky tests per test") {
                strategy.process(poolId, testResults, TestShard(tests, flakyTests = tests + tests + tests)).size shouldBe 0
            }
        }
    }
})