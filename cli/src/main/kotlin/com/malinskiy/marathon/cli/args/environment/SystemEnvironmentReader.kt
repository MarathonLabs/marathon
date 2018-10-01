package com.malinskiy.marathon.cli.args.environment

import com.malinskiy.marathon.cli.args.EnvironmentConfiguration
import java.io.File

class SystemEnvironmentReader : EnvironmentReader {
    override fun read() = EnvironmentConfiguration(System.getenv("ANDROID_HOME")?.let { File(it) })
}