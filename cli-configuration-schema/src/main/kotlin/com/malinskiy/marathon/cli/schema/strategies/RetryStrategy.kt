package com.malinskiy.marathon.cli.schema.strategies

sealed class RetryStrategy {
    object Disabled : RetryStrategy()
    data class FixedQuota(
        val totalAllowedRetryQuota: Int = 200,
        val retryPerTestQuota: Int = 3
    ) : RetryStrategy()
}
