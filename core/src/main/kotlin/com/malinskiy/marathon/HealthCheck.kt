package com.malinskiy.marathon

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

private const val DEFAULT_START_DELAY_MILLIS = 1_000L
private const val DEFAULT_TICK_DELAY_MILLIS = 1_000L

fun waitWhileTrue(startDelay: Long = DEFAULT_START_DELAY_MILLIS,
                  tickDelay: Long = DEFAULT_TICK_DELAY_MILLIS,
                  condition: suspend () -> Boolean): Job = launch {
    delay(startDelay)
    while (isActive && condition()) {
        delay(tickDelay)
    }
}
