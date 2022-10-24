package com.malinskiy.marathon.config

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class OutputConfiguration(
    @JsonProperty("maxPath") val maxPath: Int = 255
) : Serializable
