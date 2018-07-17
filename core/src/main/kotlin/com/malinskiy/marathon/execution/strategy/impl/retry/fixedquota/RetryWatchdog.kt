package com.malinskiy.marathon.execution.strategy.impl.retry.fixedquota

import java.util.concurrent.atomic.AtomicInteger

internal class RetryWatchdog(private val totalAllowedRetryQuota: Int,
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RetryWatchdog

        if (maxRetryPerTestQuota != other.maxRetryPerTestQuota) return false
        if (totalAllowedRetryQuota != other.totalAllowedRetryQuota) return false

        return true
    }

    override fun hashCode(): Int {
        var result = maxRetryPerTestQuota
        result = 31 * result + totalAllowedRetryLeft.hashCode()
        return result
    }


}
