package com.malinskiy.marathon

import com.malinskiy.marathon.execution.strategy.SortingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sorting.ExecutionTimeSortingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sorting.NoSortingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sorting.SuccessRateSortingStrategy
import groovy.lang.Closure
import java.time.Instant
import java.time.temporal.ChronoUnit

class SortingStrategyConfiguration {
    var executionTime: ExecutionTimeSortingStrategyConfiguration? = null
    var successRate: SuccessRateSortingStrategyConfiguration? = null

    fun executionTime(closure: Closure<*>) {
        executionTime = ExecutionTimeSortingStrategyConfiguration()
        closure.delegate = executionTime
        closure.call()
    }

    fun successRate(closure: Closure<*>) {
        successRate = SuccessRateSortingStrategyConfiguration()
        closure.delegate = successRate
        closure.call()
    }
}

class ExecutionTimeSortingStrategyConfiguration {
    var percentile: Double = 90.0
    var timeLimit: Instant = Instant.now().minus(30, ChronoUnit.DAYS)
}

class SuccessRateSortingStrategyConfiguration {
    var limit: Instant = Instant.now().minus(30, ChronoUnit.DAYS)
}

fun SortingStrategyConfiguration.toStrategy(): SortingStrategy = executionTime?.let {
    ExecutionTimeSortingStrategy(it.percentile, it.timeLimit)
} ?: successRate?.let {
    SuccessRateSortingStrategy(it.limit)
} ?: NoSortingStrategy()
