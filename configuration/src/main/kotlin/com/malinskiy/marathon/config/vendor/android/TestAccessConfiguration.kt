package com.malinskiy.marathon.config.vendor.android

import com.fasterxml.jackson.annotation.JsonProperty

data class TestAccessConfiguration(
    @JsonProperty("adb") val adb: Boolean = false,
    @JsonProperty("grpc") val gRPC: Boolean = false,
    @JsonProperty("console") val console: Boolean = false,
    @JsonProperty("consoleToken") val consoleToken: String = "",
)
