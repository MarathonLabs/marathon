package com.malinskiy.marathon.config.vendor.ios

import com.fasterxml.jackson.annotation.JsonProperty

data class XcresultConfiguration(
    @JsonProperty("pull") val pull: Boolean = true,
    @JsonProperty("remoteClean") val remoteClean: Boolean = true,
)
