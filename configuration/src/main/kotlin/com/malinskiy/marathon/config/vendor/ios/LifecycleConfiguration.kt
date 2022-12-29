package com.malinskiy.marathon.config.vendor.ios

import com.fasterxml.jackson.annotation.JsonProperty

data class LifecycleConfiguration(
    @JsonProperty("onPrepare") val onPrepare: Set<LifecycleAction> = emptySet(),
    @JsonProperty("shutdownUnused") val shutdownUnused: Boolean = true,
)

enum class LifecycleAction {
    @JsonProperty("TERMINATE") TERMINATE,
    @JsonProperty("ERASE") ERASE,
}
