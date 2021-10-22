package com.malinskiy.marathon.config.serialization.time

import java.time.Instant

class InstantTimeProviderImpl : InstantTimeProvider {
    override fun referenceTime(): Instant = Instant.now()
}
