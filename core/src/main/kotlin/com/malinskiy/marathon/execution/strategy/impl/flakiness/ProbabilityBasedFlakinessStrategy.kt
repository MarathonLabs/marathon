package com.malinskiy.marathon.execution.strategy.impl.flakiness

import com.malinskiy.marathon.analytics.external.MetricsProvider
import com.malinskiy.marathon.config.strategy.FlakinessStrategyConfiguration
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.strategy.FlakinessStrategy
import com.malinskiy.marathon.test.Test

/**
 * The idea is that flakiness anticipates the flakiness of the test based on the probability of test passing
 * and tries to maximize the probability of passing when executed multiple times.
 * For example the probability of test A passing is 0.5 and configuration has probability of 0.8 requested,
 * then the flakiness strategy multiplies the test A to be executed 3 times
 * (0.5 x 0.5 x 0.5 = 0.125 is the probability of all tests failing, so with probability 0.875 > 0.8 at least one of tests will pass).
 */

class ProbabilityBasedFlakinessStrategy(val cnf: FlakinessStrategyConfiguration.ProbabilityBasedFlakinessStrategyConfiguration) :
    FlakinessStrategy {

    override fun process(
        testShard: TestShard,
        metricsProvider: MetricsProvider
    ): TestShard {
        val tests = testShard.tests
        val output = mutableListOf<Test>()
        tests.forEach {
            val successRate = metricsProvider.successRate(it, cnf.timeLimit)

            if (successRate < cnf.minSuccessRate) {
                val maxFailRate = 1.0 - cnf.minSuccessRate
                var currentFailRate = 1.0 - successRate
                var counter = 0
                while (currentFailRate > maxFailRate && counter < cnf.maxCount) {
                    output.add(it)
                    currentFailRate *= currentFailRate
                    counter++
                }
            }
        }
        return testShard.copy(flakyTests = output)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProbabilityBasedFlakinessStrategy

        if (cnf.minSuccessRate != other.cnf.minSuccessRate) return false
        if (cnf.maxCount != other.cnf.maxCount) return false
        if (cnf.timeLimit != other.cnf.timeLimit) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cnf.minSuccessRate.hashCode()
        result = 31 * result + cnf.maxCount
        result = 31 * result + cnf.timeLimit.hashCode()
        return result
    }

    override fun toString(): String {
        return "ProbabilityBasedFlakinessStrategy(minSuccessRate=${cnf.minSuccessRate}, maxCount=${cnf.maxCount}, timeLimit=${cnf.timeLimit})"
    }


}

