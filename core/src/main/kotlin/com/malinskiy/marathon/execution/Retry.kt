package com.malinskiy.marathon.execution

@Suppress("TooGenericExceptionCaught")
inline fun withRetry(attempts: Int, delay: Long = 0, f: () -> Unit) {
    var attempt = 1
    while (true) {
        try {
            f()
            return
        } catch (th: Throwable) {
            if (attempt == attempts) {
                throw th
            } else {
                Thread.sleep(delay)
            }
        }
        ++attempt
    }
}
