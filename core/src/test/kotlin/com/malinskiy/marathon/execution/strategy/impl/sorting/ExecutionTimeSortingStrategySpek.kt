package com.malinskiy.marathon.execution.strategy.impl.sorting

import com.malinskiy.marathon.MetricsProviderStub
import com.malinskiy.marathon.generateTests
import com.malinskiy.marathon.execution.TestShard
import org.amshove.kluent.shouldBe
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.time.Instant
import java.time.temporal.ChronoUnit

class ExecutionTimeSortingStrategySpek : Spek({
    describe("execution-time-sorting-strategy test") {
        val instant = Instant.now()
        context("strategy with min success rate 0.8") {
            val strategy = ExecutionTimeSortingStrategy(0.8, Instant.now().minus(1, ChronoUnit.DAYS))
            group("single test shard") {
                val tests = generateTests(3)
                val testShard = TestShard(tests)
                it("should return 3 tests sorted by execution time") {
                    val metricsProvider = MetricsProviderStub(
                            executionTimes = testShard.tests.mapIndexed { index, test ->
                                Pair(test, 1000.0 + index * 1000.0)
                            }.toMap()
                    )
                    val result = testShard.tests.sortedWith(strategy.process(metricsProvider))
                    result.size shouldBe 3
                    result[0] shouldBe tests[2]
                    result[1] shouldBe tests[1]
                    result[2] shouldBe tests[0]
                }
            }
        }
    }
})
