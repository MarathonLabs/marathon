package com.malinskiy.marathon.execution.strategy.impl.retry.fixedquota

import java.util.concurrent.atomic.AtomicInteger

internal class RetryWatchdog(totalAllowedRetryQuota: Int,
                             private val maxRetryPerTestQuota: Int) {

    private val totalAllowedRetryLeft: AtomicInteger = AtomicInteger(totalAllowedRetryQuota)

    fun requestRetry(failuresCount: Int): Boolean {
        val totalAllowedRetryAvailable = totalAllowedRetryAvailable()
        val singleTestAllowed = failuresCount <= maxRetryPerTestQuota
        return totalAllowedRetryAvailable && singleTestAllowed
    }

    private fun totalAllowedRetryAvailable(): Boolean {
        return totalAllowedRetryLeft.decrementAndGet() >= 0
    }
}
