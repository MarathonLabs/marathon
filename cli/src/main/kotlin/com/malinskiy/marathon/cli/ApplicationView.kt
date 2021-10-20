package com.malinskiy.marathon.cli

import com.malinskiy.marathon.BuildConfig
import com.malinskiy.marathon.Marathon
import com.malinskiy.marathon.android.AndroidVendor
import com.malinskiy.marathon.android.adam.di.adamModule
import com.malinskiy.marathon.cli.args.MarathonCliConfiguration
import com.malinskiy.marathon.config.AppType
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.serialization.ConfigurationFactory
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.di.marathonStartKoin
import com.malinskiy.marathon.exceptions.ExceptionsReporterFactory
import com.malinskiy.marathon.ios.IOSVendor
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.usageanalytics.TrackActionType
import com.malinskiy.marathon.usageanalytics.UsageAnalytics
import com.malinskiy.marathon.usageanalytics.tracker.Event
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.SystemExitException
import com.xenomachina.argparser.mainBody
import ddmlibModule
import org.koin.core.context.stopKoin
import org.koin.dsl.module

private val logger = MarathonLogging.logger {}

fun main(args: Array<String>): Unit = mainBody(
    programName = "marathon v${BuildConfig.VERSION}"
) {
    ArgParser(args).parseInto(::MarathonCliConfiguration).run {
        logger.info { "Starting marathon v${BuildConfig.VERSION}" }
        val bugsnagExceptionsReporter = ExceptionsReporterFactory.get(bugsnagReporting)
        try {
            bugsnagExceptionsReporter.start(AppType.CLI)

            logger.info { "Checking $marathonfile config" }
            if (!marathonfile.isFile) {
                logger.error { "No config ${marathonfile.absolutePath} present" }
                throw ConfigurationException("No config ${marathonfile.absolutePath} present")
            }

            val configuration = ConfigurationFactory(marathonfileDir = marathonfile.canonicalFile.parentFile).parse(marathonfile)
            val vendorConfiguration = configuration.vendorConfiguration
            val modules = when (vendorConfiguration) {
                is VendorConfiguration.IOSConfiguration -> {
                    IOSVendor + module { single { vendorConfiguration } }
                }
                is VendorConfiguration.AndroidConfiguration -> {
                    AndroidVendor + module { single { vendorConfiguration } } + when (vendorConfiguration.vendor) {
                        VendorConfiguration.AndroidConfiguration.VendorType.ADAM -> listOf(adamModule)
                        VendorConfiguration.AndroidConfiguration.VendorType.DDMLIB -> listOf(ddmlibModule)
                    }
                }
                else -> throw ConfigurationException("No vendor config present in ${marathonfile.absolutePath}")
            }

            val application = marathonStartKoin(configuration, modules)
            val marathon: Marathon = application.koin.get()

            UsageAnalytics.enable = this.analyticsTracking
            UsageAnalytics.USAGE_TRACKER.trackEvent(Event(TrackActionType.RunType, "cli"))
            val success = marathon.run()

            val shouldReportFailure = !configuration.ignoreFailures
            if (!success && shouldReportFailure) {
                throw SystemExitException("Test run failed", 1)
            } else {
                throw SystemExitException("Test run finished", 0)
            }
        } finally {
            stopKoin()
            bugsnagExceptionsReporter.end()
        }
    }
}
