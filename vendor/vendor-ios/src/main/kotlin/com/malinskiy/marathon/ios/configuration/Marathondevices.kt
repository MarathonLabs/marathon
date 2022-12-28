package com.malinskiy.marathon.ios.configuration

import com.fasterxml.jackson.annotation.JsonProperty

data class Marathondevices(
    @JsonProperty("devices") val devices: List<AppleTarget>,
)
