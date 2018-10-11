package com.malinskiy.marathon.cli

import com.malinskiy.marathon.BuildConfig
import com.malinskiy.marathon.Marathon
import com.malinskiy.marathon.cli.args.MarathonCliConfiguration
import com.malinskiy.marathon.cli.args.environment.SystemEnvironmentReader
import com.malinskiy.marathon.cli.config.ConfigFactory
import com.malinskiy.marathon.log.MarathonLogging
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody

private val logger = MarathonLogging.logger {}

fun main(args: Array<String>): Unit = mainBody(
        programName = "marathon v${BuildConfig.VERSION}"
) {
    ArgParser(args).parseInto(::MarathonCliConfiguration).run {
        logger.info { "Starting marathon" }

        val configuration = ConfigFactory().create(
                marathonfile = marathonfile,
                environmentReader = SystemEnvironmentReader()
        )
        val marathon = Marathon(configuration = configuration)
        marathon.run()
    }
}
