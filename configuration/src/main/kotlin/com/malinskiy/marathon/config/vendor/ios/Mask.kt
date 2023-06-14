package com.malinskiy.marathon.config.vendor.ios

import com.fasterxml.jackson.annotation.JsonProperty

enum class Mask(val value: String) {
    @JsonProperty("ignored") IGNORED("ignored"),
    @JsonProperty("black") BLACK("black"),
}
