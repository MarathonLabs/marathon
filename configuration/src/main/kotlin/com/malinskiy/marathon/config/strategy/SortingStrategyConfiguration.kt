package com.malinskiy.marathon.config.strategy

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

sealed class SortingStrategyConfiguration {
    data class ExecutionTimeSortingStrategyConfiguration(
        val percentile: Double,
        val timeLimit: Instant
    ) : SortingStrategyConfiguration()
    
    object NoSortingStrategyConfiguration : SortingStrategyConfiguration()

    data class SuccessRateSortingStrategyConfiguration(
        @JsonProperty("timeLimit") val timeLimit: Instant,
        @JsonProperty("ascending") val ascending: Boolean = false
    ) : SortingStrategyConfiguration()
}
