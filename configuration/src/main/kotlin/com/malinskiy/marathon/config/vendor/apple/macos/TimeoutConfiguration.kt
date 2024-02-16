package com.malinskiy.marathon.config.vendor.apple.macos

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Duration
import java.time.temporal.ChronoUnit

/**
 * @param shell default timeout for shell commands
 * @param shellIdle default idling timeout for shell commands
 * @param reachability timeout for inactive remote host
 */
data class TimeoutConfiguration(
    @JsonProperty("shell") var shell: Duration = Duration.ofSeconds(30),
    @JsonProperty("shellIdle") var shellIdle: Duration = Duration.ofSeconds(30),
    @JsonProperty("reachability") var reachability: Duration = Duration.ofSeconds(5),
)
