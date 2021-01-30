package com.malinskiy.marathon.cli

import com.malinskiy.marathon.BuildConfig
import com.malinskiy.marathon.Marathon
import com.malinskiy.marathon.cli.args.MarathonCliConfiguration
import com.malinskiy.marathon.cli.args.VendorDecoder
import com.malinskiy.marathon.config.AppType
import com.malinskiy.marathon.di.marathonStartKoin
import com.malinskiy.marathon.exceptions.ExceptionsReporterFactory
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.usageanalytics.TrackActionType
import com.malinskiy.marathon.usageanalytics.UsageAnalytics
import com.malinskiy.marathon.usageanalytics.tracker.Event
import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.EnvironmentVariablesPropertySource
import com.sksamuel.hoplite.yaml.YamlParser
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.SystemExitException
import com.xenomachina.argparser.mainBody
import org.koin.core.context.stopKoin
import java.io.File

private val logger = MarathonLogging.logger {}

fun main(args: Array<String>): Unit = mainBody(
    programName = "marathon v${BuildConfig.VERSION}"
) {
    ArgParser(args).parseInto(::MarathonCliConfiguration).run {
        logger.info { "Starting marathon" }
        val bugsnagExceptionsReporter = ExceptionsReporterFactory.get(bugsnagReporting)
        try {
            bugsnagExceptionsReporter.start(AppType.CLI)

            val configLoader = ConfigLoader.Builder()
                .addFileExtensionMapping("yaml", YamlParser())
                .addSource(EnvironmentVariablesPropertySource(true, true))
                .addDecoder(VendorDecoder())
                .build()
            val file = File("/home/ivanbalaksha/work/marathon/sample/android-app/Marathonfile.yaml")
            val configuration = ConfigFactory(configLoader).create(file);
            val application = marathonStartKoin(configuration)
            val marathon: Marathon = application.koin.get()

            UsageAnalytics.enable = this.analyticsTracking
            UsageAnalytics.USAGE_TRACKER.trackEvent(Event(TrackActionType.RunType, "cli"))
            val success = marathon.run()

            val shouldReportFailure = !configuration.ignoreFailures
            if (!success && shouldReportFailure) {
                throw SystemExitException("Build failed", 1)
            }
        } finally {
            stopKoin()
            bugsnagExceptionsReporter.end()
        }
    }
}
