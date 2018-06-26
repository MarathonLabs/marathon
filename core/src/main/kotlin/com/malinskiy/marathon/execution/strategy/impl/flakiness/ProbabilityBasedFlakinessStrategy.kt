package com.malinskiy.marathon.execution.strategy.impl.flakiness

import com.malinskiy.marathon.analytics.metrics.MetricsProvider
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.strategy.FlakinessStrategy
import com.malinskiy.marathon.test.Test
import java.time.Instant

/**
 * The idea is that flakiness anticipates the flakiness of the test based on the probability of test passing
 * and tries to maximize the probability of passing when executed multiple times.
 * For example the probability of test A passing is 0.5 and configuration has probability of 0.8 requested,
 * then the flakiness strategy multiplies the test A to be executed 3 times
 * (0.5 x 0.5 x 0.5 = 0.125 is the probability of all tests failing, so with probability 0.875 > 0.8 at least one of tests will pass).
 */

class ProbabilityBasedFlakinessStrategy(private val minSuccessRate: Double,
                                        private val limit: Instant) : FlakinessStrategy {
    override fun process(testShard: TestShard,
                         metricsProvider: MetricsProvider): TestShard {
        val tests = testShard.tests
        val output = mutableListOf<Test>()
        tests.forEach {
            val successRate = metricsProvider.successRate(it, limit)
            if (successRate < minSuccessRate) {
                val maxFailRate = 1.0 - minSuccessRate
                var currentFailRate = 1.0 - successRate
                while (currentFailRate > maxFailRate) {
                    output.add(it)
                    currentFailRate *= currentFailRate
                }
            } else {
                output.add(it)
            }
        }
        return testShard.copy(flakyTests = output)
    }
}
