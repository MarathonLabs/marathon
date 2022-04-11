package com.malinskiy.marathon.gradle

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.util.internal.ConfigureUtil

open class RetryStrategyConfiguration {
    var fixedQuota: FixedQuotaRetryStrategyConfiguration? = null

    fun fixedQuota(action: Action<FixedQuotaRetryStrategyConfiguration>) {
        fixedQuota = FixedQuotaRetryStrategyConfiguration().also { action.execute(it) }
    }

    fun fixedQuota(closure: Closure<FixedQuotaRetryStrategyConfiguration>) = fixedQuota(ConfigureUtil.configureUsing(closure))
}

private const val DEFAULT_TOTAL_ALLOWED_RETRY_QUOTA = 200
private const val DEFAULT_RETRY_PER_TEST_QUOTA = 3

open class FixedQuotaRetryStrategyConfiguration {
    var totalAllowedRetryQuota: Int = DEFAULT_TOTAL_ALLOWED_RETRY_QUOTA
    var retryPerTestQuota: Int = DEFAULT_RETRY_PER_TEST_QUOTA
}

fun RetryStrategyConfiguration.toStrategy(): com.malinskiy.marathon.config.strategy.RetryStrategyConfiguration {
    return fixedQuota?.let {
        com.malinskiy.marathon.config.strategy.RetryStrategyConfiguration.FixedQuotaRetryStrategyConfiguration(
            it.totalAllowedRetryQuota,
            it.retryPerTestQuota
        )
    } ?: com.malinskiy.marathon.config.strategy.RetryStrategyConfiguration.NoRetryStrategyConfiguration
}
