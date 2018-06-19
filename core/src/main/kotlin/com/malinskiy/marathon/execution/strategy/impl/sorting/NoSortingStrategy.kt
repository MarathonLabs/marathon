package com.malinskiy.marathon.execution.strategy.impl.sorting

import com.malinskiy.marathon.analytics.metrics.MetricsProvider
import com.malinskiy.marathon.execution.strategy.SortingStrategy
import com.malinskiy.marathon.test.Test
import java.util.Comparator

class NoSortingStrategy : SortingStrategy {
    override fun process(metricsProvider: MetricsProvider): Comparator<Test> {
        return Comparator { _, _ -> 0 }
    }
}
