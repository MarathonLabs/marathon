package com.malinskiy.marathon.config.vendor.ios

import com.fasterxml.jackson.annotation.JsonProperty

data class RsyncConfiguration(
    @JsonProperty("remotePath") val remotePath: String = "/usr/bin/rsync"
)
