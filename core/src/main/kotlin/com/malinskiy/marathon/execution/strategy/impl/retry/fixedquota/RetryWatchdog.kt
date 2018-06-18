package com.malinskiy.marathon.execution.strategy.impl.retry.fixedquota

import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicInteger

internal class RetryWatchdog(totalAllowedRetryQuota: Int,
                             private val maxRetryPerTestCaseQuota: Int) {

    private val totalAllowedRetryLeft: AtomicInteger = AtomicInteger(totalAllowedRetryQuota)
    private val logBuilder = StringBuilder()

    private val logger = KotlinLogging.logger("RetryWatchdog")

    fun requestRetry(failuresCount: Int): Boolean {
        val totalAllowedRetryAvailable = totalAllowedRetryAvailable()
        val singleTestAllowed = failuresCount <= maxRetryPerTestCaseQuota
        val result = totalAllowedRetryAvailable && singleTestAllowed

        log(failuresCount, singleTestAllowed, result)
        return result
    }

    private fun totalAllowedRetryAvailable(): Boolean {
        return totalAllowedRetryLeft.getAndDecrement() >= 0
    }

    private fun log(testCaseFailures: Int, singleTestAllowed: Boolean, result: Boolean) {
        logBuilder.setLength(0)
        logBuilder.append("Retry requested ")
                .append(if (result) " and allowed. " else " but not allowed. ")
                .append("Total retry left :").append(totalAllowedRetryLeft.get())
                .append(" and Single Test case retry left: ")
                .append(if (singleTestAllowed) maxRetryPerTestCaseQuota - testCaseFailures else 0)
//        logger.debug(logBuilder.toString())
        logger.error(logBuilder.toString())
    }
}