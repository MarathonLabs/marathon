package com.malinskiy.marathon.cli

import com.malinskiy.marathon.BuildConfig
import com.malinskiy.marathon.Marathon
import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.cli.args.MarathonCliConfiguration
import com.malinskiy.marathon.config.AppType
import com.malinskiy.marathon.di.marathonStartKoin
import com.malinskiy.marathon.exceptions.ExceptionsReporterFactory
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.ios.IOSConfiguration
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.usageanalytics.TrackActionType
import com.malinskiy.marathon.usageanalytics.UsageAnalytics
import com.malinskiy.marathon.usageanalytics.tracker.Event
import com.malinskiy.marathon.vendor.VendorConfiguration
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.EnvironmentVariablesPropertySource
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.decoder.DataClassDecoder
import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.fp.Validated
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.yaml.YamlParser
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.SystemExitException
import com.xenomachina.argparser.mainBody
import org.koin.core.context.stopKoin
import java.io.File
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.defaultType

private val logger = MarathonLogging.logger {}


class VendorDecoder : Decoder<VendorConfiguration> {
    override fun decode(node: Node, type: KType, context: DecoderContext): ConfigResult<VendorConfiguration> {
        return when (node) {
            is MapNode -> {
                decodeType(node, type, context)
            }
            else -> ConfigFailure.DecodeError(node, type).invalid()
        }
    }

    private fun decodeType(node: MapNode, type: KType, context: DecoderContext): ConfigResult<VendorConfiguration> {
        val typeNode = node["type"]
        return when (typeNode) {
            is StringNode -> {
                createByType(typeNode.value, node, context);
            }
            else -> ConfigFailure.DecodeError(node, type).invalid()
        }
    }

    private fun createByType(vendorType: String, node: MapNode, context: DecoderContext): ConfigResult<VendorConfiguration> {
        val type = AndroidConfiguration::class.createType()
        return DataClassDecoder().decode(node, type, context).map { it as VendorConfiguration }
    }

    override fun supports(type: KType): Boolean {
        return type.classifier == VendorConfiguration::class
    }

}

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
            val configuration = configLoader.loadConfigOrThrow<Configuration>(file)

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
