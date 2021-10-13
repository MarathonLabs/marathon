package com.malinskiy.marathon.config.strategy

import java.time.Instant

sealed class FlakinessStrategyConfiguration {
    object IgnoreFlakinessStrategyConfiguration : FlakinessStrategyConfiguration()
    
    data class ProbabilityBasedFlakinessStrategyConfiguration(
        val minSuccessRate: Double,
        val maxCount: Int,
        val timeLimit: Instant
    ) : FlakinessStrategyConfiguration()
}
