package com.malinskiy.marathon.cli

import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.core.subcommands
import com.malinskiy.marathon.Marathon
import com.malinskiy.marathon.exceptions.ExceptionsReporter
import com.malinskiy.marathon.android.AndroidVendor
import com.malinskiy.marathon.android.adam.di.adamModule
import com.malinskiy.marathon.cli.args.CliConfiguration
import com.malinskiy.marathon.cli.args.MarathonCli
import com.malinskiy.marathon.cli.args.MarathonRunCommandCliConfiguration
import com.malinskiy.marathon.cli.args.Parse
import com.malinskiy.marathon.cli.args.ParseCommandCliConfiguration
import com.malinskiy.marathon.cli.args.RunMarathon
import com.malinskiy.marathon.cli.args.Version
import com.malinskiy.marathon.cli.args.VersionCommandCliConfiguration
import com.malinskiy.marathon.config.AppType
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.serialization.ConfigurationFactory
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.di.marathonStartKoin
import com.malinskiy.marathon.exceptions.ExceptionsReporterFactory
import com.malinskiy.marathon.apple.ios.IosVendor
import com.malinskiy.marathon.apple.macos.MacosVendor
import com.malinskiy.marathon.log.MarathonLogging
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.system.exitProcess

private val logger = MarathonLogging.logger {}

fun main(args: Array<String>): Unit =
    MarathonCli(::execute)
        .subcommands(Version(::execute), Parse(::execute), RunMarathon(::execute))
        .main(args)

private fun execute(cliConfiguration: CliConfiguration) {
    logger.info { "Starting marathon v${BuildConfig.VERSION}, CliConfiguration: $cliConfiguration" }
    if (cliConfiguration is VersionCommandCliConfiguration) {
        println(BuildConfig.VERSION)
        exitProcess(0)
    }

    val marathonStartConfiguration: MarathonStartConfiguration = when(cliConfiguration) {
        is MarathonRunCommandCliConfiguration -> cliConfiguration.toMarathonStartConfiguration()
        is ParseCommandCliConfiguration -> cliConfiguration.toMarathonStartConfiguration()
        else -> throw IllegalArgumentException("Please handle the new format of cliConfiguration=$cliConfiguration")
    }

    var bugsnagExceptionsReporter: ExceptionsReporter? = null;
    try {
        logger.info { "Checking ${marathonStartConfiguration.marathonfile} config" }
        if (!marathonStartConfiguration.marathonfile.isFile) {
            logger.error { "No config ${marathonStartConfiguration.marathonfile.absolutePath} present" }
            throw ConfigurationException("No config ${marathonStartConfiguration.marathonfile.absolutePath} present")
        }

        val configuration = ConfigurationFactory(
            marathonfileDir = marathonStartConfiguration.marathonfile.canonicalFile.parentFile,
            analyticsTracking = marathonStartConfiguration.analyticsTracking,
            bugsnagReporting = marathonStartConfiguration.bugsnagReporting,
        ).parse(marathonStartConfiguration.marathonfile)

        bugsnagExceptionsReporter = ExceptionsReporterFactory.get(configuration.bugsnagReporting)
        bugsnagExceptionsReporter.start(AppType.CLI)

        val vendorConfiguration = configuration.vendorConfiguration
        val modules = when (vendorConfiguration) {
            is VendorConfiguration.IOSConfiguration -> {
                IosVendor + module { single { vendorConfiguration } }
            }
            is VendorConfiguration.AndroidConfiguration -> {
                AndroidVendor + module { single { vendorConfiguration } } + listOf(adamModule)
            }
            is VendorConfiguration.MacosConfiguration -> {
                MacosVendor + module { single { vendorConfiguration } }
            }
            else -> throw ConfigurationException("No vendor config present in ${marathonStartConfiguration.marathonfile.absolutePath}")
        }

        val application = marathonStartKoin(configuration, modules)
        val marathon: Marathon = application.koin.get()

        val success = try {
            marathon.run(marathonStartConfiguration.executionCommand)
        } catch (e: Exception) {
            logger.error(e) {}
            throw PrintMessage(message = "Marathon execution crashed", statusCode = 1, printError = true)
        }

        when {
            success -> throw PrintMessage(message = "Marathon execution finished", statusCode = 0, printError = false)
            configuration.ignoreFailures -> throw PrintMessage(message = "Marathon execution finished with failures (Failures suppressed because ignoreFailures is `true`)", statusCode = 0, printError = true)
            else -> throw PrintMessage(message = "Marathon execution failed", statusCode = 1, printError = true)
        }
    } finally {
        stopKoin()
        if (bugsnagExceptionsReporter != null) {
            bugsnagExceptionsReporter.end()
        }
    }
}
