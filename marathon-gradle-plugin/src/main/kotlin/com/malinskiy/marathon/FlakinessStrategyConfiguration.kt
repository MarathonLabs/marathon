package com.malinskiy.marathon

import com.malinskiy.marathon.execution.strategy.FlakinessStrategy
import com.malinskiy.marathon.execution.strategy.impl.flakiness.IgnoreFlakinessStrategy
import com.malinskiy.marathon.execution.strategy.impl.flakiness.ProbabilityBasedFlakinessStrategy
import groovy.lang.Closure
import java.time.Instant
import java.time.temporal.ChronoUnit

class FlakinessStrategyConfiguration {
    var probabilityBased: ProbabilityBasedFlakinessStrategyConfiguration? = null

    fun probabilityBased(closure: Closure<*>) {
        probabilityBased = ProbabilityBasedFlakinessStrategyConfiguration()
        closure.delegate = probabilityBased
        closure.call()
    }
}

class ProbabilityBasedFlakinessStrategyConfiguration {
    var minSuccessRate: Double = 0.8
    var maxCount: Int = 3
    var timeLimit: Instant = Instant.now().minus(30, ChronoUnit.DAYS)
}

fun FlakinessStrategyConfiguration.toStrategy(): FlakinessStrategy = probabilityBased?.let {
    ProbabilityBasedFlakinessStrategy(it.minSuccessRate, it.maxCount, it.timeLimit)
} ?: IgnoreFlakinessStrategy()
