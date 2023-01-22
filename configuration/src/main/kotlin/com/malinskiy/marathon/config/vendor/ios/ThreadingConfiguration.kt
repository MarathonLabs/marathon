package com.malinskiy.marathon.config.vendor.ios

import com.fasterxml.jackson.annotation.JsonProperty

data class ThreadingConfiguration(
    @JsonProperty("deviceProviderThreads") val deviceProviderThreads: Int = 8,
    @JsonProperty("deviceThreads") val deviceThreads: Int = 2,
)
