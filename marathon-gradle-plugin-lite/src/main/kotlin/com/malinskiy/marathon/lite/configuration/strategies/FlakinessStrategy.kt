package com.malinskiy.marathon.lite.configuration.strategies

import java.io.Serializable
import java.time.Instant

sealed class FlakinessStrategy : Serializable {
    object Disabled : FlakinessStrategy()
    data class ProbabilityBased(val minSuccessRate: Double, val maxCount: Int, val timeLimit: Instant) : FlakinessStrategy()
}
