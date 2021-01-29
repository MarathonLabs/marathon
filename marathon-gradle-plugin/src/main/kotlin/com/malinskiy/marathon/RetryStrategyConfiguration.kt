package com.malinskiy.marathon

import com.malinskiy.marathon.execution.strategy.FixedQuotaRetryStrategy
import com.malinskiy.marathon.execution.strategy.NoRetryStrategy
import com.malinskiy.marathon.execution.strategy.RetryStrategy
import groovy.lang.Closure

open class RetryStrategyConfiguration {
    var fixedQuota: FixedQuotaRetryStrategyConfiguration? = null

    fun fixedQuota(block: FixedQuotaRetryStrategyConfiguration.() -> Unit) {
        fixedQuota = FixedQuotaRetryStrategyConfiguration().also(block)
    }

    fun fixedQuota(closure: Closure<*>) {
        fixedQuota = FixedQuotaRetryStrategyConfiguration()
        closure.delegate = fixedQuota
        closure.call()
    }
}

private const val DEFAULT_TOTAL_ALLOWED_RETRY_QUOTA = 200
private const val DEFAULT_RETRY_PER_TEST_QUOTA = 3

open class FixedQuotaRetryStrategyConfiguration {
    var totalAllowedRetryQuota: Int = DEFAULT_TOTAL_ALLOWED_RETRY_QUOTA
    var retryPerTestQuota: Int = DEFAULT_RETRY_PER_TEST_QUOTA
}

fun RetryStrategyConfiguration.toStrategy(): RetryStrategy {
    return fixedQuota?.let {
        FixedQuotaRetryStrategy(it.totalAllowedRetryQuota, it.retryPerTestQuota)
    } ?: NoRetryStrategy()
}
