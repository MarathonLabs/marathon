package com.malinskiy.marathon.gradle

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.util.internal.ConfigureUtil
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Random

class SortingStrategyConfiguration {
    var executionTime: ExecutionTimeSortingStrategyConfiguration? = null
    var randomOrder: RandomOrderStrategyConfiguration? = null
    var successRate: SuccessRateSortingStrategyConfiguration? = null


    fun executionTime(action: Action<ExecutionTimeSortingStrategyConfiguration>) {
        executionTime = ExecutionTimeSortingStrategyConfiguration().also { action.execute(it) }
    }

    fun executionTime(closure: Closure<ExecutionTimeSortingStrategyConfiguration>) = executionTime(ConfigureUtil.configureUsing(closure))

    fun randomOrder(action: Action<RandomOrderStrategyConfiguration>) {
        randomOrder = RandomOrderStrategyConfiguration().also { action.execute(it) }
    }

    fun randomOrder(closure: Closure<RandomOrderStrategyConfiguration>) = randomOrder(ConfigureUtil.configureUsing(closure))

    fun successRate(action: Action<SuccessRateSortingStrategyConfiguration>) {
        successRate = SuccessRateSortingStrategyConfiguration().also { action.execute(it) }
    }

    fun successRate(closure: Closure<SuccessRateSortingStrategyConfiguration>) = successRate(ConfigureUtil.configureUsing(closure))
}

private const val DEFAULT_PERCENTILE = 90.0
const val DEFAULT_DAYS_COUNT = 30L

class ExecutionTimeSortingStrategyConfiguration {
    var percentile: Double = DEFAULT_PERCENTILE
    var timeLimit: Instant = Instant.now().minus(DEFAULT_DAYS_COUNT, ChronoUnit.DAYS)
}

class SuccessRateSortingStrategyConfiguration {
    var limit: Instant = Instant.now().minus(DEFAULT_DAYS_COUNT, ChronoUnit.DAYS)
    var ascending: Boolean = false
}

class RandomOrderStrategyConfiguration {}

fun SortingStrategyConfiguration.toStrategy(): com.malinskiy.marathon.config.strategy.SortingStrategyConfiguration = executionTime?.let {
    com.malinskiy.marathon.config.strategy.SortingStrategyConfiguration.ExecutionTimeSortingStrategyConfiguration(
        it.percentile,
        it.timeLimit
    )
} ?: successRate?.let {
    com.malinskiy.marathon.config.strategy.SortingStrategyConfiguration.SuccessRateSortingStrategyConfiguration(it.limit, it.ascending)
} ?: randomOrder?.let {
    com.malinskiy.marathon.config.strategy.SortingStrategyConfiguration.RandomOrderStrategyConfiguration
} ?: com.malinskiy.marathon.config.strategy.SortingStrategyConfiguration.NoSortingStrategyConfiguration
