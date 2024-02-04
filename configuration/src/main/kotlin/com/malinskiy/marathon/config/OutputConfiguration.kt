package com.malinskiy.marathon.config

import com.fasterxml.jackson.annotation.JsonProperty

// Value 0 is equivalent to unlimited path length
const val OUTPUT_MAX_PATH = 0
// Value 0 is equivalent to unlimited filename length
const val OUTPUT_MAX_FILENAME = 255

data class OutputConfiguration(
    @JsonProperty("maxPath") val maxPath: Int = OUTPUT_MAX_PATH,
    @JsonProperty("maxFilename") val maxFilename: Int = OUTPUT_MAX_FILENAME,
)
