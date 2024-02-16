package com.malinskiy.marathon.config.vendor.apple.ios

import com.fasterxml.jackson.annotation.JsonProperty

enum class Display(val value: String) {
    @JsonProperty("internal") INTERNAL("internal"),
    @JsonProperty("external") EXTERNAL("external"),
}
