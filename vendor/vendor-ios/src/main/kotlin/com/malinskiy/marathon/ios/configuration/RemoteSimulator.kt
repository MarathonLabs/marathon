package com.malinskiy.marathon.ios.configuration

import com.fasterxml.jackson.annotation.JsonProperty

data class RemoteSimulator(
    @JsonProperty("addr") val addr: String,
    @JsonProperty("port") val port: Int = 22,
    @JsonProperty("udid") val udid: String,
    @JsonProperty("username") val username: String?
)
