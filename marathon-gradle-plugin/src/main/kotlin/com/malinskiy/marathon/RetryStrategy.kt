package com.malinskiy.marathon

import com.malinskiy.marathon.execution.strategy.impl.retry.NoRetryStrategy
import groovy.lang.Closure
import org.gradle.api.Action
import com.malinskiy.marathon.execution.strategy.impl.retry.fixedquota.FixedQuotaRetryStrategy as RealFixedQuotaRetryStrategy
import com.malinskiy.marathon.execution.strategy.RetryStrategy as RealRetryStrategy

open class RetryStrategy {
    var fixedQuota: FixedQuotaRetryStrategy? = null
    fun fixedQuota(closure: Closure<*>) {
        fixedQuota = FixedQuotaRetryStrategy()
        closure.delegate = fixedQuota
    }
}

open class FixedQuotaRetryStrategy {
    var totalAllowedRetryQuota: Int = 200
    var retryPerTestQuota: Int = 3
}

fun RetryStrategy.toRealStrategy(): RealRetryStrategy {
    return fixedQuota?.let {
        RealFixedQuotaRetryStrategy(it.totalAllowedRetryQuota, it.retryPerTestQuota)
    } ?: NoRetryStrategy()
}