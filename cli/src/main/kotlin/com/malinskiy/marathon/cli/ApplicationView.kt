package com.malinskiy.marathon.cli

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.malinskiy.marathon.BuildConfig
import com.malinskiy.marathon.Marathon
import com.malinskiy.marathon.usageanalytics.TrackActionType
import com.malinskiy.marathon.usageanalytics.UsageAnalytics
import com.malinskiy.marathon.usageanalytics.tracker.Event
import com.malinskiy.marathon.cli.args.MarathonCliConfiguration
import com.malinskiy.marathon.cli.args.environment.SystemEnvironmentReader
import com.malinskiy.marathon.cli.config.ConfigFactory
import com.malinskiy.marathon.cli.config.DeserializeModule
import com.malinskiy.marathon.cli.config.time.InstantTimeProviderImpl
import com.malinskiy.marathon.log.MarathonLogging
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.SystemExitException
import com.xenomachina.argparser.mainBody
import kotlin.system.exitProcess

private val logger = MarathonLogging.logger {}

fun main(args: Array<String>): Unit = mainBody(
        programName = "marathon v${BuildConfig.VERSION}"
) {
    ArgParser(args).parseInto(::MarathonCliConfiguration).run {
        logger.info { "Starting marathon" }

        val mapper = ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID))
        mapper.registerModule(DeserializeModule(InstantTimeProviderImpl()))
                .registerModule(KotlinModule())
                .registerModule(JavaTimeModule())

        val configuration = ConfigFactory(mapper).create(
                marathonfile = marathonfile,
                environmentReader = SystemEnvironmentReader()
        )
        val marathon = Marathon(configuration = configuration)
        UsageAnalytics.enable = this.analyticsTracking
        UsageAnalytics.tracker.trackEvent(Event(TrackActionType.RunType, "cli"))
        val success = marathon.run()
        if (!success && !configuration.ignoreFailures) {
            throw SystemExitException("Build failed", 1)
        }
        success
    }
    exitProcess(0)
}
