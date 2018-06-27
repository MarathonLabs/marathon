package com.malinskiy.marathon.execution.strategy.impl.flakiness

import com.malinskiy.marathon.analytics.metrics.MetricsProvider
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.strategy.FlakinessStrategy

class IgnoreFlakinessStrategy : FlakinessStrategy {
    override fun process(testShard: TestShard, metricsProvider: MetricsProvider): TestShard {
        return testShard
    }
}
