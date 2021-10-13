package com.malinskiy.marathon.config.strategy

import com.fasterxml.jackson.annotation.JsonProperty

sealed class RetryStrategyConfiguration {
    data class FixedQuotaRetryStrategyConfiguration(
        @JsonProperty("totalAllowedRetryQuota") val totalAllowedRetryQuota: Int = 200,
        @JsonProperty("retryPerTestQuota") val retryPerTestQuota: Int = 3
    ) : RetryStrategyConfiguration()

    object NoRetryStrategyConfiguration : RetryStrategyConfiguration()
}
