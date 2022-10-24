package com.malinskiy.marathon.config.vendor.android

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class TestAccessConfiguration(
    @JsonProperty("adb") val adb: Boolean = false,
    @JsonProperty("grpc") val grpc: Boolean = false,
    @JsonProperty("console") val console: Boolean = false,
    @JsonProperty("consoleToken") val consoleToken: String = "",
) : Serializable
