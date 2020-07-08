package com.malinskiy.marathon.cli.args

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import java.io.File

class MarathonCliConfiguration(parser: ArgParser) {
    val marathonfile: File by parser
        .storing("--marathonfile", "-m", help = "marathonfile file path") { File(this) }
        .default(File("Marathonfile"))
    val analyticsTracking: Boolean by parser
        .storing("--analyticsTracking", help = "Enable anonymous analytics tracking") { this.toBoolean() }
        .default<Boolean>(false)
    val bugsnagReporting: Boolean by parser
        .storing("--bugsnag", help = "Enable/Disable anonymous crash reporting. Enabled by default") { this.toBoolean() }
        .default(true)
}
