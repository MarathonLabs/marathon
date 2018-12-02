package com.malinskiy.marathon.execution

import kotlinx.coroutines.delay

@Suppress("TooGenericExceptionCaught")
suspend fun withRetry(attempts: Int, delayTime: Long = 0, f: suspend () -> Unit) {
    var attempt = 1
    while (true) {
        try {
            f()
            return
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
