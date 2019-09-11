package com.malinskiy.marathon.time

import java.time.Clock

class SystemTimer(private val clock: Clock) : Timer {
    override val startTimeMillis = clock.millis()
    override val elapsedTimeMillis: Long
        get() = clock.millis() - startTimeMillis

    override fun currentTimeMillis() = clock.millis()
    override fun measure(block: () -> Unit) = measureTimeMillis(block)

    /**
     * Executes the given [block] and returns elapsed time in milliseconds.
     */
    private fun measureTimeMillis(block: () -> Unit): Long {
        val start = clock.millis()
        block()
        return clock.millis() - start
    }
}