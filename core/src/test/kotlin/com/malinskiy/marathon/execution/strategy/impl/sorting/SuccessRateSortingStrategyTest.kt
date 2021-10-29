package com.malinskiy.marathon.execution.strategy.impl.sorting

import com.malinskiy.marathon.MetricsProviderStub
import com.malinskiy.marathon.config.strategy.SortingStrategyConfiguration
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.extension.toSortingStrategy
import com.malinskiy.marathon.generateTests
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class SuccessRateSortingStrategyTest {
    @Test
    fun `strategy with default ordering, single test shard, should return 3 tests sorted by descending success rate`() {
        val strategy = SortingStrategyConfiguration.SuccessRateSortingStrategyConfiguration(Instant.now().minus(1, ChronoUnit.DAYS))
            .toSortingStrategy()
        val tests = generateTests(3)
        val testShard = TestShard(tests)

        val metricsProvider = MetricsProviderStub(
            successRates = testShard.tests.mapIndexed { index, test ->
                Pair(test, 0.1 * index)
            }.toMap()
        )
        val result = testShard.tests.sortedWith(strategy.process(metricsProvider))
        result.size shouldBe 3
        result[0] shouldBe tests[2]
        result[1] shouldBe tests[1]
        result[2] shouldBe tests[0]
    }

    @Test
    fun `strategy with ascending ordering, single test shard, should return 3 tests sorted by descending success rate`() {
        val strategy = SortingStrategyConfiguration.SuccessRateSortingStrategyConfiguration(
            Instant.now().minus(1, ChronoUnit.DAYS),
            ascending = true
        ).toSortingStrategy()

        val tests = generateTests(3)
        val testShard = TestShard(tests)

        val metricsProvider = MetricsProviderStub(
            successRates = testShard.tests.mapIndexed { index, test ->
                Pair(test, 0.1 * index)
            }.toMap()
        )
        val result = testShard.tests.sortedWith(strategy.process(metricsProvider))
        result.size shouldBe 3
        result[0] shouldBe tests[0]
        result[1] shouldBe tests[1]
        result[2] shouldBe tests[2]
    }

    @Test
    fun `strategy with descending ordering, single test shard, should return 3 tests sorted by descending success rate`() {
        val strategy = SortingStrategyConfiguration.SuccessRateSortingStrategyConfiguration(
            Instant.now().minus(1, ChronoUnit.DAYS),
            ascending = false
        ).toSortingStrategy()
        val tests = generateTests(3)
        val testShard = TestShard(tests)
        val metricsProvider = MetricsProviderStub(
            successRates = testShard.tests.mapIndexed { index, test ->
                Pair(test, 0.1 * index)
            }.toMap()
        )
        val result = testShard.tests.sortedWith(strategy.process(metricsProvider))
        result.size shouldBe 3
        result[0] shouldBe tests[2]
        result[1] shouldBe tests[1]
        result[2] shouldBe tests[0]
    }
}
