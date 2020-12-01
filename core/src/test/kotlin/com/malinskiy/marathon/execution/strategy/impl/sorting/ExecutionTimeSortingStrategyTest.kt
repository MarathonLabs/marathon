package com.malinskiy.marathon.execution.strategy.impl.sorting

import com.malinskiy.marathon.MetricsProviderStub
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.generateTests
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class ExecutionTimeSortingStrategyTest {
    val instant: Instant = Instant.now()
    val strategy = ExecutionTimeSortingStrategy(0.8, Instant.now().minus(1, ChronoUnit.DAYS))

    @Test
    fun `strategy with min success rate 0_8, single test shard, should return 3 tests sorted by execution time`() {
        val tests = generateTests(3)
        val testShard = TestShard(tests)

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
