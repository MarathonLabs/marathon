package com.malinskiy.marathon.config.vendor.apple.ios

import com.fasterxml.jackson.annotation.JsonProperty

data class LifecycleConfiguration(
    @JsonProperty("shutdownUnused") val shutdownUnused: Boolean = true,
    @JsonProperty("onPrepare") val onPrepare: Set<LifecycleAction> = emptySet(),
    @JsonProperty("onDispose") val onDispose: Set<LifecycleAction> = emptySet(),
)

enum class LifecycleAction {
    /**
     * Forcefully terminate simulator via kill -9
     * 
     * If you use this then simulator might not boot next time unless you erase it on start
     */
    @JsonProperty("TERMINATE") TERMINATE,

    /**
     * Soft termination of simulator via simctl shutdown
     */
    @JsonProperty("SHUTDOWN") SHUTDOWN,

    /**
     * Erase simulator. Implies shutdown
     */
    @JsonProperty("ERASE") ERASE,
}
