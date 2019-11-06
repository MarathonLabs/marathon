package com.malinskiy.marathon.config

import com.malinskiy.marathon.execution.Configuration

interface ConfigurationValidator {
    fun validate(configuration: Configuration)
}