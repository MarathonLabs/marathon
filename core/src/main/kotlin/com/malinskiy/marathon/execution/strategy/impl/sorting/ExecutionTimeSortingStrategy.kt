package com.malinskiy.marathon.execution.strategy.impl.sorting

import com.malinskiy.marathon.analytics.metrics.MetricsProvider
import com.malinskiy.marathon.execution.strategy.SortingStrategy
import com.malinskiy.marathon.test.Test
import java.time.Instant
import java.util.Comparator

class ExecutionTimeSortingStrategy(val percentile: Double,
                                   val timeLimit: Instant) : SortingStrategy {

    override fun process(metricsProvider: MetricsProvider): Comparator<Test> =
            Comparator.comparingDouble<Test> {
                metricsProvider.executionTime(it, percentile, timeLimit)
            }.reversed()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExecutionTimeSortingStrategy

        if (percentile != other.percentile) return false
        if (timeLimit != other.timeLimit) return false

        return true
    }

    override fun hashCode(): Int {
        var result = percentile.hashCode()
        result = 31 * result + timeLimit.hashCode()
        return result
    }
}

private fun Long.toInstant(): Instant = Instant.now().plusMillis(this)
