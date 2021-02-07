package com.malinskiy.marathon.cli.schema.strategies

import java.time.Instant

sealed class FlakinessStrategy {
    object Disabled : FlakinessStrategy()
    data class ProbabilityBased(val minSuccessRate: Double, val maxCount: Int, val timeLimit: Instant) : FlakinessStrategy()
}
