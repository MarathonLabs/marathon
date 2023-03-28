package com.malinskiy.marathon.cli

import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.core.subcommands
import com.malinskiy.marathon.Marathon
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
import com.malinskiy.marathon.ios.AppleVendor
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.usageanalytics.TrackActionType
import com.malinskiy.marathon.usageanalytics.UsageAnalytics
import com.malinskiy.marathon.usageanalytics.tracker.Event
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

    val bugsnagExceptionsReporter = ExceptionsReporterFactory.get(marathonStartConfiguration.bugsnagReporting)
    try {
        bugsnagExceptionsReporter.start(AppType.CLI)

        logger.info { "Checking ${marathonStartConfiguration.marathonfile} config" }
        if (!marathonStartConfiguration.marathonfile.isFile) {
            logger.error { "No config ${marathonStartConfiguration.marathonfile.absolutePath} present" }
            throw ConfigurationException("No config ${marathonStartConfiguration.marathonfile.absolutePath} present")
        }

        val configuration = ConfigurationFactory(
            marathonfileDir = marathonStartConfiguration.marathonfile.canonicalFile.parentFile
        ).parse(marathonStartConfiguration.marathonfile)
        val vendorConfiguration = configuration.vendorConfiguration
        val modules = when (vendorConfiguration) {
            is VendorConfiguration.IOSConfiguration -> {
                AppleVendor + module { single { vendorConfiguration } }
            }
            is VendorConfiguration.AndroidConfiguration -> {
                AndroidVendor + module { single { vendorConfiguration } } + listOf(adamModule)
            }
            else -> throw ConfigurationException("No vendor config present in ${marathonStartConfiguration.marathonfile.absolutePath}")
        }

        val application = marathonStartKoin(configuration, modules)
        val marathon: Marathon = application.koin.get()

        UsageAnalytics.enable = marathonStartConfiguration.analyticsTracking
        UsageAnalytics.USAGE_TRACKER.trackEvent(Event(TrackActionType.RunType, "cli"))
        val success = marathon.run(marathonStartConfiguration.executionCommand)

        val shouldReportFailure = !configuration.ignoreFailures

        when {
            success -> throw PrintMessage(message = "Marathon execution finished", error = false)
            shouldReportFailure -> throw PrintMessage(message = "Marathon execution finished with failures (Failures suppressed because ignoreFailures is `true`)", error = false)
            else -> throw PrintMessage(message = "Marathon execution failed", error = true)
        }
    } finally {
        stopKoin()
        bugsnagExceptionsReporter.end()
    }
}
