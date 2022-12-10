package com.malinskiy.marathon.config.vendor.ios

import java.time.Duration

data class TimeoutConfiguration(
    var reachability: Duration = Duration.ofSeconds(5),
)
