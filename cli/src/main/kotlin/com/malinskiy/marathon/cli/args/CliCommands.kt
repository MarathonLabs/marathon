package com.malinskiy.marathon.cli.args

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import java.io.File

class MarathonRunCommonOptions : OptionGroup() {
    val marathonfile by option("--marathonfile", "-m", help="Marathonfile file path")
        .file()
        .default(File("Marathonfile"))
    val analyticsTracking by option("--analyticsTracking", help="Enable / Disable anonymous analytics tracking. Enabled by default.")
        .convert { it.toBoolean() }
        .default(true)
    val bugsnagReporting by option("--bugsnag", help="Enable/Disable anonymous crash reporting. Enabled by default")
        .convert { it.toBoolean() }
        .default(true)
}

class MarathonCli(
    private val starter: (CliConfiguration) -> Unit
) : CliktCommand(invokeWithoutSubcommand = true, name = "marathon") {

    private val marathonRunCommonOptions by MarathonRunCommonOptions()

    override fun run() {
        val subcommand = currentContext.invokedSubcommand

        // there are subcommands further, it means we should dive into particular subcommands
        if (subcommand != null) return

        // this code is here to support the previous versions of CLI where there are not subcommands
        val marathonRunCommandCliConfiguration = MarathonRunCommandCliConfiguration(
            marathonfile = marathonRunCommonOptions.marathonfile,
            analyticsTracking = marathonRunCommonOptions.analyticsTracking,
            bugsnagReporting = marathonRunCommonOptions.bugsnagReporting
        )
        starter(marathonRunCommandCliConfiguration)
    }
}

class Version(
    private val starter: (CliConfiguration) -> Unit
) : CliktCommand(name = "version", help="Print version and exit") {
    override fun run() {
        starter(VersionCommandCliConfiguration)
    }
}

class Parse(
    private val starter: (CliConfiguration) -> Unit
): CliktCommand(name = "parse", help="Print the list of tests without executing them") {

    private val marathonfile by option("--marathonfile", "-m", help="Marathonfile file path")
        .file()
        .default(File("Marathonfile"))
    private val parseOutputFileName by option("--output", "-o", help="Output file name in yaml format")
    private val includeFlakyTests by option("--include-flaky-tests", "-f", help="Include/Exclude flaky tests that will be run too in the output")
        .convert { it.toBoolean() }
        .default(false)
    override fun run() {
        val parseCommandCliConfiguration = ParseCommandCliConfiguration(
            marathonfile = marathonfile,
            outputFileName = parseOutputFileName,
            includeFlakyTests = includeFlakyTests,
        )
        starter(parseCommandCliConfiguration)
    }
}

class RunMarathon(
    private val starter: (CliConfiguration) -> Unit
) : CliktCommand(name = "run", help="Run Marathon to execute tests") {

    private val marathonRunCommonOptions by MarathonRunCommonOptions()

    override fun run() {
        val marathonRunCommandCliConfiguration = MarathonRunCommandCliConfiguration(
            marathonfile = marathonRunCommonOptions.marathonfile,
            analyticsTracking = marathonRunCommonOptions.analyticsTracking,
            bugsnagReporting = marathonRunCommonOptions.bugsnagReporting
        )
        starter(marathonRunCommandCliConfiguration)
    }
}
