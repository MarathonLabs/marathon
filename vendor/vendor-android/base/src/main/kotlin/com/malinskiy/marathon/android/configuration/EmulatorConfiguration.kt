package com.malinskiy.marathon.android.configuration

data class EmulatorConfiguration(
    val reversePortForwarding: ReversePortForwarding = ReversePortForwarding()
)

data class ReversePortForwarding(
    val gRPC: Boolean = false,
    val console: Boolean = false,
    val adb: Boolean = false
)
