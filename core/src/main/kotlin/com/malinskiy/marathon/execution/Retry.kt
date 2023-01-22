package com.malinskiy.marathon.execution

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

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

fun <T> withRetrySync(attempts: Int, delayTime: Long = 0, f: () -> T): T {
    var attempt = 1
    while (true) {
        try {
            return f()
        } catch (th: Throwable) {
            if (attempt == attempts) {
                throw th
            } else {
                Thread.sleep(delayTime)
            }
        }
        ++attempt
    }
}
