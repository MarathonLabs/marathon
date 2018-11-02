package com.malinskiy.marathon.execution.strategy.impl.sorting

import com.fasterxml.jackson.annotation.JsonProperty
import com.malinskiy.marathon.analytics.metrics.MetricsProvider
import com.malinskiy.marathon.exceptions.ConfigurationException
import com.malinskiy.marathon.execution.strategy.SortingStrategy
import com.malinskiy.marathon.test.Test
import java.time.Instant
import java.util.Comparator

class ExecutionTimeSortingStrategy(@JsonProperty("percentile") private val percentile: Double,
                                   @JsonProperty("timeLimit") private val timeLimitInstant: Instant? = null,
                                   @JsonProperty("timeLimitMillis") private val timeLimitMillis: Long? = null) : SortingStrategy {
    val timeLimit: Instant = timeLimitInstant
            ?: timeLimitMillis?.toInstant()
            ?: throw ConfigurationException("Either timeLimit or timeLimitMillis must be specified")

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
