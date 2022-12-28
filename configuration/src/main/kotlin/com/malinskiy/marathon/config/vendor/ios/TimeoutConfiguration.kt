package com.malinskiy.marathon.config.vendor.ios

import java.time.Duration
import java.time.temporal.ChronoUnit

/**
 * @param shell default timeout for shell commands
 * @param shellIdle default idling timeout for shell commands
 * @param reachability timeout for inactive remote host
 * @param testDestination waiting timeout for xcodebuild test destination device. granularity is to a second
 */
data class TimeoutConfiguration(
    var shell: Duration = Duration.ofSeconds(30),
    var shellIdle: Duration = Duration.ofSeconds(30),
    var reachability: Duration = Duration.ofSeconds(5),
    var screenshot: Duration = Duration.ofSeconds(10),
    var video: Duration = Duration.ofSeconds(300),
    var erase: Duration = shell,
    var shutdown: Duration = shell,
    val delete: Duration = shutdown,
    val create: Duration = Duration.ofSeconds(30),
    var boot: Duration = shell,
    var install: Duration = shell,
    var uninstall: Duration = shell,
    var testDestination: Duration = Duration.ofSeconds(30),
) {
    
    companion object {
        val INFINITE: Duration = Duration.of(7, ChronoUnit.DAYS)
    }
}
