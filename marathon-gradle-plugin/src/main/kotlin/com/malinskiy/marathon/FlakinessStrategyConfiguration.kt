package com.malinskiy.marathon

import com.malinskiy.marathon.execution.strategy.FlakinessStrategy
import com.malinskiy.marathon.execution.strategy.impl.flakiness.IgnoreFlakinessStrategy
import com.malinskiy.marathon.execution.strategy.impl.flakiness.ProbabilityBasedFlakinessStrategy
import org.gradle.api.Action
import java.io.Serializable
import java.time.Instant
import java.time.temporal.ChronoUnit

class FlakinessStrategyConfiguration : Serializable {
    var probabilityBased: ProbabilityBasedFlakinessStrategyConfiguration? = null

    fun probabilityBased(action: Action<ProbabilityBasedFlakinessStrategyConfiguration>) {
        probabilityBased = ProbabilityBasedFlakinessStrategyConfiguration().also(action::execute)
    }
}

private const val DEFAULT_MIN_SUCCESS_RATE = 0.8
private const val DEFAULT_MAX_FLAKY_TESTS_COUNT = 3

class ProbabilityBasedFlakinessStrategyConfiguration : Serializable{
    var minSuccessRate: Double = DEFAULT_MIN_SUCCESS_RATE
    var maxCount: Int = DEFAULT_MAX_FLAKY_TESTS_COUNT
    var limit: Instant = Instant.now().minus(DEFAULT_DAYS_COUNT, ChronoUnit.DAYS)
}

fun FlakinessStrategyConfiguration.toStrategy(): FlakinessStrategy = probabilityBased?.let {
    ProbabilityBasedFlakinessStrategy(it.minSuccessRate, it.maxCount, it.limit)
} ?: IgnoreFlakinessStrategy()
