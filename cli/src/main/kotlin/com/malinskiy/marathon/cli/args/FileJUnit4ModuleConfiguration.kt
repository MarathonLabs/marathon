package com.malinskiy.marathon.cli.args

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.File

class FileJUnit4ModuleConfiguration(
    @JsonProperty("applicationClasspath") val applicationClasspath: List<File>?,
    @JsonProperty("testClasspath") val testClasspath: List<File>?,
)
