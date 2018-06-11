package com.malinskiy.marathon.execution.strategy.impl.flakiness

import com.malinskiy.marathon.analytics.metrics.MetricsProvider
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.strategy.FlakinessStrategy
import com.malinskiy.marathon.test.Test

class ProbabilityBasedFlakinessStrategy(private val minSuccessRate: Double) : FlakinessStrategy {
    override fun process(testShard: TestShard,
                         metricsProvider: MetricsProvider): TestShard {
        val tests = testShard.tests
        val output = mutableListOf<Test>()
        tests.forEach {
            output.add(it)
            val successRate = metricsProvider.successRate(it)
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
        return TestShard(output)
    }

}