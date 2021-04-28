package com.malinskiy.marathon

import com.malinskiy.marathon.execution.strategy.RetryStrategy
import com.malinskiy.marathon.execution.strategy.impl.retry.NoRetryStrategy
import com.malinskiy.marathon.execution.strategy.impl.retry.fixedquota.FixedQuotaRetryStrategy
import org.gradle.api.Action
import java.io.Serializable

open class RetryStrategyConfiguration : Serializable {
    var fixedQuota: FixedQuotaRetryStrategyConfiguration? = null

    fun fixedQuota(action: Action<FixedQuotaRetryStrategyConfiguration>) {
        fixedQuota = FixedQuotaRetryStrategyConfiguration().also(action::execute)
    }
}

private const val DEFAULT_TOTAL_ALLOWED_RETRY_QUOTA = 200
private const val DEFAULT_RETRY_PER_TEST_QUOTA = 3

open class FixedQuotaRetryStrategyConfiguration : Serializable{
    var totalAllowedRetryQuota: Int = DEFAULT_TOTAL_ALLOWED_RETRY_QUOTA
    var retryPerTestQuota: Int = DEFAULT_RETRY_PER_TEST_QUOTA
}

fun RetryStrategyConfiguration.toStrategy(): RetryStrategy {
    return fixedQuota?.let {
        FixedQuotaRetryStrategy(it.totalAllowedRetryQuota, it.retryPerTestQuota)
    } ?: NoRetryStrategy()
}
