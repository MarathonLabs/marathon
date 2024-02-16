package com.malinskiy.marathon.config.vendor.apple.ios

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Duration
import java.time.temporal.ChronoUnit

/**
 * @param shell default timeout for shell commands
 * @param shellIdle default idling timeout for shell commands
 * @param reachability timeout for inactive remote host
 * @param testDestination waiting timeout for xcodebuild test destination device. granularity is to a second
 */
data class TimeoutConfiguration(
    @JsonProperty("shell") var shell: Duration = Duration.ofSeconds(30),
    @JsonProperty("shellIdle") var shellIdle: Duration = Duration.ofSeconds(30),
    @JsonProperty("reachability") var reachability: Duration = Duration.ofSeconds(5),
    @JsonProperty("screenshot") var screenshot: Duration = Duration.ofSeconds(10),
    @JsonProperty("video") var video: Duration = Duration.ofSeconds(300),
    @JsonProperty("erase") var erase: Duration = shell,
    @JsonProperty("shutdown") var shutdown: Duration = shell,
    @JsonProperty("delete") val delete: Duration = shutdown,
    @JsonProperty("create") val create: Duration = Duration.ofSeconds(30),
    @JsonProperty("boot") var boot: Duration = shell,
    @JsonProperty("install") var install: Duration = shell,
    @JsonProperty("uninstall") var uninstall: Duration = shell,
    @JsonProperty("testDestination") var testDestination: Duration = Duration.ofSeconds(30),
) {
    
    companion object {
        val INFINITE: Duration = Duration.of(7, ChronoUnit.DAYS)
    }
}
