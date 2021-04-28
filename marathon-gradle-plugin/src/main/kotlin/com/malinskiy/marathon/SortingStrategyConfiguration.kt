package com.malinskiy.marathon

import com.malinskiy.marathon.execution.strategy.SortingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sorting.ExecutionTimeSortingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sorting.NoSortingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sorting.SuccessRateSortingStrategy
import org.gradle.api.Action
import java.io.Serializable
import java.time.Instant
import java.time.temporal.ChronoUnit

class SortingStrategyConfiguration : Serializable {
    var executionTime: ExecutionTimeSortingStrategyConfiguration? = null
    var successRate: SuccessRateSortingStrategyConfiguration? = null

    fun executionTime(action: Action<ExecutionTimeSortingStrategyConfiguration>) {
        executionTime = ExecutionTimeSortingStrategyConfiguration().also(action::execute)
    }

    fun successRate(action: Action<SuccessRateSortingStrategyConfiguration>) {
        successRate = SuccessRateSortingStrategyConfiguration().also(action::execute)
    }
}

private const val DEFAULT_PERCENTILE = 90.0
const val DEFAULT_DAYS_COUNT = 30L

class ExecutionTimeSortingStrategyConfiguration : Serializable{
    var percentile: Double = DEFAULT_PERCENTILE
    var limit: Instant = Instant.now().minus(DEFAULT_DAYS_COUNT, ChronoUnit.DAYS)
}

class SuccessRateSortingStrategyConfiguration: Serializable {
    var limit: Instant = Instant.now().minus(DEFAULT_DAYS_COUNT, ChronoUnit.DAYS)
    var ascending: Boolean = false
}

fun SortingStrategyConfiguration.toStrategy(): SortingStrategy = executionTime?.let {
    ExecutionTimeSortingStrategy(it.percentile, it.limit)
} ?: successRate?.let {
    SuccessRateSortingStrategy(it.limit, it.ascending)
} ?: NoSortingStrategy()
