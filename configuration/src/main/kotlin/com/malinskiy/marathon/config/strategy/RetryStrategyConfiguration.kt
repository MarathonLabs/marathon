package com.malinskiy.marathon.config.strategy

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = RetryStrategyConfiguration.FixedQuotaRetryStrategyConfiguration::class, name = "fixed-quota"),
    JsonSubTypes.Type(value = RetryStrategyConfiguration.NoRetryStrategyConfiguration::class, name = "no-retry"),
)
sealed class RetryStrategyConfiguration {
    data class FixedQuotaRetryStrategyConfiguration(
        @JsonProperty("totalAllowedRetryQuota") val totalAllowedRetryQuota: Int = 200,
        @JsonProperty("retryPerTestQuota") val retryPerTestQuota: Int = 3
    ) : RetryStrategyConfiguration()

    object NoRetryStrategyConfiguration : RetryStrategyConfiguration()
}
