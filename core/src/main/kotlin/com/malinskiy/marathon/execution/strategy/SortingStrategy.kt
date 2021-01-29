package com.malinskiy.marathon.execution.strategy

import com.malinskiy.marathon.analytics.external.MetricsProvider
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import java.time.Instant

sealed class SortingStrategy {
    abstract fun process(metricsProvider: MetricsProvider): Comparator<Test>
}

class ExecutionTimeSortingStrategy(
    val percentile: Double,
    val timeLimit: Instant
) : SortingStrategy() {

    val logger = MarathonLogging.logger(ExecutionTimeSortingStrategy::class.java.simpleName)

    override fun process(metricsProvider: MetricsProvider): java.util.Comparator<Test> =
        java.util.Comparator.comparingDouble<Test> {
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

class NoSortingStrategy : SortingStrategy() {
    override fun process(metricsProvider: MetricsProvider): java.util.Comparator<Test> {
        return Comparator { _, _ -> 0 }
    }

    override fun hashCode() = javaClass.canonicalName.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        val javaClass: Class<Any> = other.javaClass
        return this.javaClass.canonicalName == javaClass.canonicalName
    }

    override fun toString(): String {
        return "NoSortingStrategy()"
    }


}


class SuccessRateSortingStrategy(
    private val timeLimit: Instant,
    private val ascending: Boolean = false
) : SortingStrategy() {
    override fun process(metricsProvider: MetricsProvider): java.util.Comparator<Test> {
        val comparator = java.util.Comparator.comparingDouble<Test>
        {
            metricsProvider.successRate(it, timeLimit)
        }

        return when (ascending) {
            true -> comparator
            false -> comparator.reversed()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SuccessRateSortingStrategy

        if (timeLimit != other.timeLimit) return false
        if (ascending != other.ascending) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timeLimit.hashCode()
        result = 31 * result + ascending.hashCode()
        return result
    }


}
