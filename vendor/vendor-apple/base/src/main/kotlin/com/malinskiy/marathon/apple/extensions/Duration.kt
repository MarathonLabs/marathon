package com.malinskiy.marathon.apple.extensions

import java.time.Duration
import java.time.temporal.ChronoUnit

class Durations {
    companion object {
        val INFINITE = Duration.of(7, ChronoUnit.DAYS)
    }
}
