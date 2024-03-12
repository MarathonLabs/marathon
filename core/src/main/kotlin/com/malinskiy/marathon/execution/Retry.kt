package com.malinskiy.marathon.execution

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import mu.KLogger

@Suppress("TooGenericExceptionCaught")
suspend fun <T> withRetry(attempts: Int, delayTime: Long = 0, f: suspend () -> T): T {
    var attempt = 1
    while (true) {
        try {
            return f()
        } catch (e: CancellationException) {
            throw e
        } catch (th: Throwable) {
            if (attempt == attempts) {
                throw th
            } else {
                delay(delayTime)
            }
        }
        ++attempt
    }
}

fun <T> withRetrySync(attempts: Int, delayTime: Long = 0, logger: KLogger, f: () -> T): T {
    var attempt = 1
    while (true) {
        try {
            return f()
        } catch (th: Throwable) {
            if (attempt == attempts) {
                throw th
            } else {
                logger.warn(th) { "Retrying after failure" }
                Thread.sleep(delayTime)
            }
        }
        ++attempt
    }
}
