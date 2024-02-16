package com.malinskiy.marathon.apple.configuration

import com.fasterxml.jackson.annotation.JsonProperty

data class Marathondevices(
    @JsonProperty("workers") val workers: List<Worker>,
)

data class Worker(
    @JsonProperty("transport") val transport: Transport,
    @JsonProperty("devices") val devices: List<AppleTarget>,
)
