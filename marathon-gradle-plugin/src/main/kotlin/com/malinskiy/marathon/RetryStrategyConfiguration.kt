package com.malinskiy.marathon

import com.malinskiy.marathon.execution.strategy.impl.retry.NoRetryStrategy
import groovy.lang.Closure
import com.malinskiy.marathon.execution.strategy.impl.retry.fixedquota.FixedQuotaRetryStrategy
import com.malinskiy.marathon.execution.strategy.RetryStrategy

open class RetryStrategyConfiguration {
    var fixedQuota: FixedQuotaRetryStrategyConfiguration? = null
    fun fixedQuota(closure: Closure<*>) {
        fixedQuota = FixedQuotaRetryStrategyConfiguration()
        closure.delegate = fixedQuota
    }
}

open class FixedQuotaRetryStrategyConfiguration {
    var totalAllowedRetryQuota: Int = 200
    var retryPerTestQuota: Int = 3
}

fun RetryStrategyConfiguration.toStrategy(): RetryStrategy {
    return fixedQuota?.let {
        FixedQuotaRetryStrategy(it.totalAllowedRetryQuota, it.retryPerTestQuota)
    } ?: NoRetryStrategy()
}