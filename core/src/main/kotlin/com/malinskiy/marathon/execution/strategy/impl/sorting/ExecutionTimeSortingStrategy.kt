package com.malinskiy.marathon.execution.strategy.impl.sorting

import com.malinskiy.marathon.analytics.metrics.MetricsProvider
import com.malinskiy.marathon.execution.strategy.SortingStrategy
import com.malinskiy.marathon.test.Test

class ExecutionTimeSortingStrategy(private val percentile: Double) : SortingStrategy {
    override fun process(tests: Collection<Test>, metricsProvider: MetricsProvider): Collection<Test> {
        return tests.sortedBy { metricsProvider.executionTime(it, percentile) }
    }
}