package com.malinskiy.marathon.config

import com.fasterxml.jackson.annotation.JsonProperty

const val OUTPUT_MAX_PATH = 0
const val OUTPUT_MAX_FILENAME = 255

data class OutputConfiguration(
    @JsonProperty("maxPath") val maxPath: Int = OUTPUT_MAX_PATH,
    @JsonProperty("maxFilename") val maxFilename: Int = OUTPUT_MAX_FILENAME,
)
