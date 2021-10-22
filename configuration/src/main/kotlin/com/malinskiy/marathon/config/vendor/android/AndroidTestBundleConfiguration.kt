package com.malinskiy.marathon.config.vendor.android

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.File

data class AndroidTestBundleConfiguration(
    @JsonProperty("application") val application: File?,
    @JsonProperty("testApplication") val testApplication: File
)
