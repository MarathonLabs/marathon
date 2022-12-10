package com.malinskiy.marathon.ios.configuration

import com.fasterxml.jackson.annotation.JsonProperty

data class Marathondevices(
    @JsonProperty("remote") val remote: List<RemoteSimulator>?,
    @JsonProperty("local") val local: List<LocalSimulator>?,
)
