package com.malinskiy.marathon.execution.strategy.impl.sorting

import com.malinskiy.marathon.analytics.external.MetricsProvider
import com.malinskiy.marathon.config.strategy.SortingStrategyConfiguration
import com.malinskiy.marathon.execution.strategy.SortingStrategy
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test

class ExecutionTimeSortingStrategy(private val cnf: SortingStrategyConfiguration.ExecutionTimeSortingStrategyConfiguration) :
    SortingStrategy {

    val logger = MarathonLogging.logger(ExecutionTimeSortingStrategy::class.java.simpleName)

    override fun process(metricsProvider: MetricsProvider): Comparator<Test> =
        Comparator.comparingDouble<Test> {
            val expectedDuration = metricsProvider.executionTime(it, cnf.percentile, cnf.timeLimit)
            expectedDuration
        }.reversed()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExecutionTimeSortingStrategy

        if (cnf.percentile != other.cnf.percentile) return false
        if (cnf.timeLimit != other.cnf.timeLimit) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cnf.percentile.hashCode()
        result = 31 * result + cnf.timeLimit.hashCode()
        return result
    }

    override fun toString(): String {
        return "ExecutionTimeSortingStrategy(percentile=${cnf.percentile}, timeLimit=${cnf.timeLimit}, logger=$logger)"
    }


}
