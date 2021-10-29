package com.malinskiy.marathon.config.strategy

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.Instant

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = BatchingStrategyConfiguration.FixedSizeBatchingStrategyConfiguration::class, name = "fixed-size"),
    JsonSubTypes.Type(value = BatchingStrategyConfiguration.IsolateBatchingStrategyConfiguration::class, name = "isolate"),
)
sealed class BatchingStrategyConfiguration {
    data class FixedSizeBatchingStrategyConfiguration(
        val size: Int,
        val durationMillis: Long? = null,
        val percentile: Double? = null,
        val timeLimit: Instant? = null,
        val lastMileLength: Int = 0
    ) : BatchingStrategyConfiguration()

    object IsolateBatchingStrategyConfiguration : BatchingStrategyConfiguration()
}
