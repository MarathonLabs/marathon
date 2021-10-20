package com.malinskiy.marathon.config.serialization.time

import java.time.Instant

interface InstantTimeProvider {
    fun referenceTime(): Instant
}
