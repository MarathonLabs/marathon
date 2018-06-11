package com.malinskiy.marathon.execution.strategy

import com.malinskiy.marathon.analytics.metrics.MetricsProvider
import com.malinskiy.marathon.test.Test

interface SortingStrategy {
    fun process(tests: Collection<Test>,
                metricsProvider: MetricsProvider): Collection<Test>
}
