package com.malinskiy.marathon.gradle

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.util.internal.ConfigureUtil
import java.time.Instant
import java.time.temporal.ChronoUnit

class FlakinessStrategyConfiguration {
    var probabilityBased: ProbabilityBasedFlakinessStrategyConfiguration? = null

    fun probabilityBased(action: Action<ProbabilityBasedFlakinessStrategyConfiguration>) {
        probabilityBased = ProbabilityBasedFlakinessStrategyConfiguration().also { action.execute(it) }
    }

    fun probabilityBased(closure: Closure<ProbabilityBasedFlakinessStrategyConfiguration>) =
        probabilityBased(ConfigureUtil.configureUsing(closure))
}

private const val DEFAULT_MIN_SUCCESS_RATE = 0.8
private const val DEFAULT_MAX_FLAKY_TESTS_COUNT = 3

class ProbabilityBasedFlakinessStrategyConfiguration {
    var minSuccessRate: Double = DEFAULT_MIN_SUCCESS_RATE
    var maxCount: Int = DEFAULT_MAX_FLAKY_TESTS_COUNT
    var timeLimit: Instant = Instant.now().minus(DEFAULT_DAYS_COUNT, ChronoUnit.DAYS)
}

fun FlakinessStrategyConfiguration.toStrategy(): com.malinskiy.marathon.config.strategy.FlakinessStrategyConfiguration =
    probabilityBased?.let {
        com.malinskiy.marathon.config.strategy.FlakinessStrategyConfiguration.ProbabilityBasedFlakinessStrategyConfiguration(
            it.minSuccessRate,
            it.maxCount,
            it.timeLimit
        )
    } ?: com.malinskiy.marathon.config.strategy.FlakinessStrategyConfiguration.IgnoreFlakinessStrategyConfiguration
