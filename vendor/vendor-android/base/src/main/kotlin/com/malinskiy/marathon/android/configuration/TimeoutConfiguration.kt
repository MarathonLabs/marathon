package com.malinskiy.marathon.android.configuration

import java.time.Duration


data class TimeoutConfiguration(
    val shell: Duration = Duration.ofSeconds(20),
    val listFiles: Duration = shell,
    val pushFile: Duration = Duration.ofSeconds(60),
    val pullFile: Duration = Duration.ofSeconds(30),
    val uninstall: Duration = shell,
    val install: Duration = shell,
    val screenrecorder: Duration = Duration.ofMinutes(10),
    val screencapturer: Duration = Duration.ofMillis(300),
)
