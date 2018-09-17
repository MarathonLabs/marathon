package com.malinskiy.marathon.execution.strategy.impl.sorting

import com.fasterxml.jackson.annotation.JsonProperty
import com.malinskiy.marathon.analytics.metrics.MetricsProvider
import com.malinskiy.marathon.execution.strategy.SortingStrategy
import com.malinskiy.marathon.test.Test
import java.time.Instant
import java.util.*

class SuccessRateSortingStrategy(@JsonProperty("limit") private val limit: Instant) : SortingStrategy {
    override fun process(metricsProvider: MetricsProvider): Comparator<Test> =
            Comparator.comparingDouble<Test> {
                metricsProvider.successRate(it, limit)
            }.reversed()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SuccessRateSortingStrategy

        if (limit != other.limit) return false

        return true
    }

    override fun hashCode(): Int {
        return limit.hashCode()
    }


}
