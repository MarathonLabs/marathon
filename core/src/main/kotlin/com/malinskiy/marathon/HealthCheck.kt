package com.malinskiy.marathon

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

fun healthCheck(startDelay: Int, tickDelay: Int, f: () -> Boolean): Job = launch {
    delay(startDelay)
    while (f()) {
        delay(tickDelay)
    }
}
