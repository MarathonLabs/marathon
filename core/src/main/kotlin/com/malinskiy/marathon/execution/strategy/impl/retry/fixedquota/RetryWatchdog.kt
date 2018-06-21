package com.malinskiy.marathon.execution.strategy.impl.retry.fixedquota

import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicInteger

internal class RetryWatchdog(totalAllowedRetryQuota: Int,
                             private val maxRetryPerTestQuota: Int) {

    private val totalAllowedRetryLeft: AtomicInteger = AtomicInteger(totalAllowedRetryQuota)

    private val logger = KotlinLogging.logger("RetryWatchdog")

    fun requestRetry(failuresCount: Int): Boolean {
        val totalAllowedRetryAvailable = totalAllowedRetryAvailable()
        val singleTestAllowed = failuresCount <= maxRetryPerTestQuota
        val result = totalAllowedRetryAvailable && singleTestAllowed

        log(failuresCount, singleTestAllowed, result)
        return result
    }

    private fun totalAllowedRetryAvailable(): Boolean {
        return totalAllowedRetryLeft.decrementAndGet() >= 0
    }

    private fun log(testCaseFailures: Int, singleTestAllowed: Boolean, result: Boolean) {
        logger.warn("""
            Retry requested ${if (result) "and allowed. " else "but not allowed. "}
            Total retry left: ${totalAllowedRetryLeft.get()} and Single Test retry left: ${if (singleTestAllowed) maxRetryPerTestQuota - testCaseFailures else 0}
        """.trimIndent())
    }
}
