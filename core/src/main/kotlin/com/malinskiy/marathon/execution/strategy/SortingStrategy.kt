package com.malinskiy.marathon.execution.strategy

import com.malinskiy.marathon.analytics.metrics.MetricsProvider
import com.malinskiy.marathon.test.Test
import java.util.Comparator

interface SortingStrategy {
    fun process(metricsProvider: MetricsProvider): Comparator<Test>
}
