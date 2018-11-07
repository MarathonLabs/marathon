package com.malinskiy.marathon.cli.config.time

import java.time.Instant

class InstantTimeProviderImpl : InstantTimeProvider {
    override fun referenceTime(): Instant = Instant.now()
}
