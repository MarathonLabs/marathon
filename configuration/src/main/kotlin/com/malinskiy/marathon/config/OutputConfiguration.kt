package com.malinskiy.marathon.config

import com.fasterxml.jackson.annotation.JsonProperty

data class OutputConfiguration(
    @JsonProperty("maxPath") val maxPath: Int = 255
)
