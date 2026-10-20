package com.malinskiy.marathon.config.vendor.android

import java.time.Duration

data class TimeoutConfiguration(
    var shell: Duration = Duration.ofSeconds(20),
    var listFiles: Duration = shell,
    var pushFile: Duration = Duration.ofSeconds(60),
    var pushFolder: Duration = Duration.ofSeconds(60),
    var pullFile: Duration = Duration.ofSeconds(30),
    var uninstall: Duration = shell,
    var install: Duration = Duration.ofSeconds(60),
    var screenrecorder: Duration = Duration.ofSeconds(200),
    // Applies per frame, and a frame that exceeds it is dropped. A full framebuffer pull over a
    // remote adb connection (e.g. through a tunnel) can take seconds.
    var screencapturer: Duration = Duration.ofSeconds(5),
    var socketIdleTimeout: Duration = Duration.ofSeconds(30),
    var portForward: Duration = shell,
    var boot: Duration = Duration.ofSeconds(30),
)
