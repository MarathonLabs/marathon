package com.malinskiy.marathon.config

interface ConfigurationValidator {
    fun validate(configuration: Configuration)
}
