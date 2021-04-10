package com.malinskiy.marathon.cli.schema.strategies

import java.io.Serializable

sealed class RetryStrategy : Serializable {
    object Disabled : RetryStrategy()
    data class FixedQuota(
        val totalAllowedRetryQuota: Int = 200,
        val retryPerTestQuota: Int = 3
    ) : RetryStrategy()
}
