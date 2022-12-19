package com.malinskiy.marathon.config.vendor.ios

import com.fasterxml.jackson.annotation.JsonProperty

data class LifecycleConfiguration(
    @JsonProperty("onStart") val onStart: Set<LifecycleAction> = emptySet(),
)

enum class LifecycleAction {
    @JsonProperty("TERMINATE") TERMINATE,
    @JsonProperty("ERASE") ERASE,
}
