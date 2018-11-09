package com.malinskiy.marathon.cli.config.time

import java.time.Instant

interface InstantTimeProvider {
    fun referenceTime(): Instant
}
