package com.malinskiy.marathon.execution.strategy.impl.sorting

import com.fasterxml.jackson.annotation.JsonProperty
import com.malinskiy.marathon.analytics.metrics.MetricsProvider
import com.malinskiy.marathon.execution.strategy.SortingStrategy
import com.malinskiy.marathon.test.Test
import java.time.Instant
import java.util.*

class SuccessRateSortingStrategy(@JsonProperty("timeLimit") private val timeLimit: Instant,
                                 @JsonProperty("reverse") private val reverse: Boolean = false) : SortingStrategy {
    override fun process(metricsProvider: MetricsProvider): Comparator<Test> {
        val comparator = Comparator.comparingDouble<Test>
        {
            metricsProvider.successRate(it, timeLimit)
        }

        return when(reverse) {
            true -> comparator
            false -> comparator.reversed()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SuccessRateSortingStrategy

        if (timeLimit != other.timeLimit) return false
        if (reverse != other.reverse) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timeLimit.hashCode()
        result = 31 * result + reverse.hashCode()
        return result
    }


}
