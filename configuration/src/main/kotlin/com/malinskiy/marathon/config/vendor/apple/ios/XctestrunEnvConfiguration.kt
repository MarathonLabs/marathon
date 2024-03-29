package com.malinskiy.marathon.config.vendor.apple.ios

import com.fasterxml.jackson.annotation.JsonProperty

data class XctestrunEnvConfiguration(
    @JsonProperty("app") val appEnvs: Map<String, String> = emptyMap(),
    @JsonProperty("test") val testEnvs: Map<String, String> = emptyMap(),
)
