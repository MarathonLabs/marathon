package com.malinskiy.marathon.execution.strategy.impl.sorting

import com.malinskiy.marathon.analytics.metrics.MetricsProvider
import com.malinskiy.marathon.execution.strategy.SortingStrategy
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toSimpleSafeTestName
import java.time.Instant
import java.util.*

class ExecutionTimeSortingStrategy(val percentile: Double,
                                   val timeLimit: Instant) : SortingStrategy {

    val logger = MarathonLogging.logger(ExecutionTimeSortingStrategy::class.java.simpleName)

    override fun process(metricsProvider: MetricsProvider): Comparator<Test> =
            Comparator.comparingDouble<Test> {
                val expectedDuration = metricsProvider.executionTime(it, percentile, timeLimit)
                expectedDuration
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

    override fun toString(): String {
        return "ExecutionTimeSortingStrategy(percentile=$percentile, timeLimit=$timeLimit, logger=$logger)"
    }


}