package com.malinskiy.marathon.cli

import com.malinskiy.marathon.cli.args.MarathonRunCommandCliConfiguration
import com.malinskiy.marathon.cli.args.ParseCommandCliConfiguration
import com.malinskiy.marathon.config.ExecutionCommand
import com.malinskiy.marathon.config.MarathonRunCommand
import com.malinskiy.marathon.config.ParseCommand
import java.io.File

data class MarathonStartConfiguration(
    val marathonfile: File,
    val analyticsTracking: Boolean,
    val bugsnagReporting: Boolean,
    val executionCommand: ExecutionCommand
)

fun MarathonRunCommandCliConfiguration.toMarathonStartConfiguration(): MarathonStartConfiguration =
    MarathonStartConfiguration(
        marathonfile = this.marathonfile,
        bugsnagReporting = this.bugsnagReporting,
        analyticsTracking = this.analyticsTracking,
        executionCommand = MarathonRunCommand,
    )

fun ParseCommandCliConfiguration.toMarathonStartConfiguration(): MarathonStartConfiguration =
    MarathonStartConfiguration(
        marathonfile = this.marathonfile,
        bugsnagReporting = false,
        analyticsTracking = false,
        executionCommand = ParseCommand(this.outputFileName, this.includeFlakyTests),
    )
