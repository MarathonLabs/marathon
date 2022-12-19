package com.malinskiy.marathon.ios.configuration

import com.fasterxml.jackson.annotation.JsonProperty
import com.malinskiy.marathon.config.vendor.ios.SshAuthentication

data class RemoteSimulator(
    @JsonProperty("addr") val addr: String,
    @JsonProperty("port") val port: Int = 22,
    @JsonProperty("udid") val udid: String,
    @JsonProperty("authentication") val authentication: SshAuthentication? = null
)
