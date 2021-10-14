package com.malinskiy.marathon.config.strategy

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.Instant

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = SortingStrategyConfiguration.ExecutionTimeSortingStrategyConfiguration::class, name = "execution-time"),
    JsonSubTypes.Type(value = SortingStrategyConfiguration.NoSortingStrategyConfiguration::class, name = "no-sorting"),
    JsonSubTypes.Type(value = SortingStrategyConfiguration.SuccessRateSortingStrategyConfiguration::class, name = "success-rate"),
)
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
