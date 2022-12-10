package com.malinskiy.marathon.ios.configuration

import com.fasterxml.jackson.annotation.JsonProperty

data class LocalSimulator(
    @JsonProperty("udid") val udid: String,
)
