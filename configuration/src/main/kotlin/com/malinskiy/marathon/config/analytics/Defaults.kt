package com.malinskiy.marathon.config.analytics

import java.time.Duration

data class Defaults(val successRate: Double = .0, val duration: Duration = Duration.ofMinutes(5))
