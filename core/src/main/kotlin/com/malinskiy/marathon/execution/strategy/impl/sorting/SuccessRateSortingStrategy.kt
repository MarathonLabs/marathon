package com.malinskiy.marathon.execution.strategy.impl.sorting

import com.malinskiy.marathon.analytics.external.MetricsProvider
import com.malinskiy.marathon.config.strategy.SortingStrategyConfiguration
import com.malinskiy.marathon.execution.strategy.SortingStrategy
import com.malinskiy.marathon.test.Test

class SuccessRateSortingStrategy(private val cnf: SortingStrategyConfiguration.SuccessRateSortingStrategyConfiguration) : SortingStrategy {
    override fun process(metricsProvider: MetricsProvider): Comparator<Test> {
        val comparator = Comparator.comparingDouble<Test>
        {
            metricsProvider.successRate(it, cnf.timeLimit)
        }

        return when (cnf.ascending) {
            true -> comparator
            false -> comparator.reversed()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SuccessRateSortingStrategy

        if (cnf.timeLimit != other.cnf.timeLimit) return false
        if (cnf.ascending != other.cnf.ascending) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cnf.timeLimit.hashCode()
        result = 31 * result + cnf.ascending.hashCode()
        return result
    }
}
