package com.malinskiy.marathon.execution.strategy

import com.malinskiy.marathon.analytics.external.MetricsProvider
import com.malinskiy.marathon.execution.TestShard

interface FlakinessStrategy {
    fun process(testShard: TestShard,
                metricsProvider: MetricsProvider): TestShard
}
