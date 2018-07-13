package com.malinskiy.marathon.cli

import com.malinskiy.marathon.BuildConfig
import com.malinskiy.marathon.Marathon
import com.malinskiy.marathon.cli.args.MarathonCliConfiguration
import com.malinskiy.marathon.cli.config.ConfigFactory
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) = mainBody(
        programName = "marathon v${BuildConfig.VERSION}"
) {
    ArgParser(args).parseInto(::MarathonCliConfiguration).run {
        logger.info { "Starting marathon" }

        val configuration = ConfigFactory().create(marathonfile, androidSdkDir)
        val marathon = Marathon(configuration = configuration)
        marathon.run()
    }
    return@mainBody
}