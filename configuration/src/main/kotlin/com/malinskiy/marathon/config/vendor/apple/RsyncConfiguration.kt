package com.malinskiy.marathon.config.vendor.apple

import com.fasterxml.jackson.annotation.JsonProperty

data class RsyncConfiguration(
    @JsonProperty("remotePath") val remotePath: String = "/usr/bin/rsync"
)
