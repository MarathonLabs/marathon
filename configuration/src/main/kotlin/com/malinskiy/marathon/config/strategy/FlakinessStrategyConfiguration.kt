package com.malinskiy.marathon.config.strategy

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.io.Serializable
import java.time.Instant

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = FlakinessStrategyConfiguration.IgnoreFlakinessStrategyConfiguration::class, name = "ignore"),
    JsonSubTypes.Type(value = FlakinessStrategyConfiguration.ProbabilityBasedFlakinessStrategyConfiguration::class, name = "probability"),
)
sealed class FlakinessStrategyConfiguration : Serializable {
    object IgnoreFlakinessStrategyConfiguration : FlakinessStrategyConfiguration()

    data class ProbabilityBasedFlakinessStrategyConfiguration(
        val minSuccessRate: Double,
        val maxCount: Int,
        val timeLimit: Instant
    ) : FlakinessStrategyConfiguration()
}
