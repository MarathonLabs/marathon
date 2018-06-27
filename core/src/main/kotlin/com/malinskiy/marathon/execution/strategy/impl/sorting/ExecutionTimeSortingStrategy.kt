package com.malinskiy.marathon.execution.strategy.impl.sorting

import com.malinskiy.marathon.analytics.metrics.MetricsProvider
import com.malinskiy.marathon.execution.strategy.SortingStrategy
import com.malinskiy.marathon.test.Test
import java.time.Instant

class ExecutionTimeSortingStrategy(private val percentile: Double,
                                   private val limit: Instant) : SortingStrategy {
    override fun process(metricsProvider: MetricsProvider): Comparator<Test> =
            Comparator.comparingDouble<Test> {
                metricsProvider.executionTime(it, percentile, limit)
            }.reversed()
}
