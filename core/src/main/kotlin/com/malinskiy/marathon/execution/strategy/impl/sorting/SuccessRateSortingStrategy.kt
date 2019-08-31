package com.malinskiy.marathon.execution.strategy.impl.sorting

import com.fasterxml.jackson.annotation.JsonProperty
import com.malinskiy.marathon.analytics.external.MetricsProvider
import com.malinskiy.marathon.execution.strategy.SortingStrategy
import com.malinskiy.marathon.test.Test
import java.time.Instant
import java.util.*

class SuccessRateSortingStrategy(
    @JsonProperty("timeLimit") private val timeLimit: Instant,
    @JsonProperty("ascending") private val ascending: Boolean = false
) : SortingStrategy {
    override fun process(metricsProvider: MetricsProvider): Comparator<Test> {
        val comparator = Comparator.comparingDouble<Test>
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
