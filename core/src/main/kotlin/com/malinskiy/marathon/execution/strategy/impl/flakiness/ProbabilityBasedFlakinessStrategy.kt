package com.malinskiy.marathon.execution.strategy.impl.flakiness

import com.fasterxml.jackson.annotation.JsonProperty
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

class ProbabilityBasedFlakinessStrategy(@JsonProperty("minSuccessRate") private val minSuccessRate: Double,
                                        @JsonProperty("maxCount") private  val maxCount: Int,
                                        @JsonProperty("timeLimit") private  val timeLimit: Instant) : FlakinessStrategy {
    override fun process(testShard: TestShard,
                         metricsProvider: MetricsProvider): TestShard {
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
            } else {
                output.add(it)
            }
        }
        return testShard.copy(flakyTests = output)
    }
}
