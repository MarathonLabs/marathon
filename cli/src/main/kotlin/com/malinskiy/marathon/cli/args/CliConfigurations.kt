package com.malinskiy.marathon.cli.args

import java.io.File

sealed class CliConfiguration

object VersionCommandCliConfiguration : CliConfiguration()

data class MarathonRunCommandCliConfiguration(
    val marathonfile: File,
    val analyticsTracking: Boolean,
    val bugsnagReporting: Boolean
) : CliConfiguration()

data class ParseCommandCliConfiguration(
    val marathonfile: File,
    val outputFile: File?
) : CliConfiguration()
