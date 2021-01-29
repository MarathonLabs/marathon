package com.malinskiy.marathon.execution.strategy

import com.malinskiy.marathon.analytics.external.MetricsProvider
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.test.Test
import java.time.Instant

sealed class FlakinessStrategy {
    abstract fun process(
        testShard: TestShard,
        metricsProvider: MetricsProvider
    ): TestShard
}

class IgnoreFlakinessStrategy : FlakinessStrategy() {
    override fun process(testShard: TestShard, metricsProvider: MetricsProvider): TestShard {
        return testShard
    }
}


/**
 * The idea is that flakiness anticipates the flakiness of the test based on the probability of test passing
 * and tries to maximize the probability of passing when executed multiple times.
 * For example the probability of test A passing is 0.5 and configuration has probability of 0.8 requested,
 * then the flakiness strategy multiplies the test A to be executed 3 times
 * (0.5 x 0.5 x 0.5 = 0.125 is the probability of all tests failing, so with probability 0.875 > 0.8 at least one of tests will pass).
 */
data class ProbabilityBasedFlakinessStrategy(
    val minSuccessRate: Double,
    val maxCount: Int,
    val timeLimit: Instant
) : FlakinessStrategy() {
    override fun process(
        testShard: TestShard,
        metricsProvider: MetricsProvider
    ): TestShard {
        val tests = testShard.tests
        val output = mutableListOf<Test>()
        tests.forEach {
            val successRate = metricsProvider.successRate(it, timeLimit)

            if (successRate < minSuccessRate) {
                val maxFailRate = 1.0 - minSuccessRate
                var currentFailRate = 1.0 - successRate
                var counter = 0
                while (currentFailRate > maxFailRate && counter < maxCount) {
                    output.add(it)
                    currentFailRate *= currentFailRate
                    counter++
                }
            }
        }
        return testShard.copy(flakyTests = output)
    }
}
