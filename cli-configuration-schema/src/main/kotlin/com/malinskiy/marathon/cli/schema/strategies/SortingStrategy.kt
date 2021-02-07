package com.malinskiy.marathon.cli.schema.strategies

import java.time.Instant

sealed class SortingStrategy {
    object Disabled : SortingStrategy()
    data class SuccessRate(val timeLimit: Instant, val ascending: Boolean = false) : SortingStrategy()
    data class ExecutionTime(val percentile: Double, val timeLimit: Instant) : SortingStrategy()
}
