package com.malinskiy.marathon.config.vendor.android

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.File

data class ProfilingConfiguration(
    @JsonProperty("enabled") val enabled: Boolean = false,
    @JsonProperty("pbtxt") val pbtxt: File? = null,
    @JsonProperty("urlTemplate") val urlTemplate: String = "https://cloud.marathonlabs.io/"
)
