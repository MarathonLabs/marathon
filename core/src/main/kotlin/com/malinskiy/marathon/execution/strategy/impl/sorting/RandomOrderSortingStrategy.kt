package com.malinskiy.marathon.execution.strategy.impl.sorting

import com.malinskiy.marathon.analytics.external.MetricsProvider
import com.malinskiy.marathon.execution.strategy.SortingStrategy
import com.malinskiy.marathon.test.Test
import kotlin.random.Random

class RandomOrderSortingStrategy : SortingStrategy {
    private val cache = mutableMapOf<Test, Int>()

    override fun process(metricsProvider: MetricsProvider): Comparator<Test> {
        return Comparator.comparingInt {
            cache.computeIfAbsent(it) { Random.nextInt() }
        }
    }
}
