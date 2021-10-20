package com.malinskiy.marathon.config.environment


interface EnvironmentReader {
    fun read(): EnvironmentConfiguration
}
