package com.malinskiy.marathon.cli.args.environment

import com.malinskiy.marathon.cli.args.EnvironmentConfiguration

interface EnvironmentReader {
    fun read(): EnvironmentConfiguration
}