package com.malinskiy.marathon.ios.bin.xcrun.simctl.model

import com.fasterxml.jackson.annotation.JsonProperty

data class SimctlRuntime(
    @JsonProperty("bundlePath") val bundle: String?,
    @JsonProperty("buildversion") val buildVersion: String,
    @JsonProperty("availability") val isAvailable: String,
    @JsonProperty("runtimeRoot") val runtimeRoot: String?,
    @JsonProperty("name") val name: String,
    @JsonProperty("identifier") val identifier: String,
    @JsonProperty("version") val version: String,
)
