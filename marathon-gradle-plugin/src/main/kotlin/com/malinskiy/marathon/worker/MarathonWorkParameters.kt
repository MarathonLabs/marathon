package com.malinskiy.marathon.worker

import com.malinskiy.marathon.MarathonRunner
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.properties.MarathonProperties

data class MarathonWorkParameters(
    val marathonFactory: () -> MarathonRunner,
    val configuration: Configuration,
    val properties: MarathonProperties
)
