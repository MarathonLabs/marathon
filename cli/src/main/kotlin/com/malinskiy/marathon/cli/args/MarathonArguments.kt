package com.malinskiy.marathon.cli.args

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import java.io.File

class MarathonArguments(parser: ArgParser) {
    val verbose by parser.flagging("-v", "--verbose", help = "enable verbose mode")
    val marathonfile by parser.storing("--marathonfile", "-m", help = "marathonfile file path") { File(this) }.default("Marathonfile")
    val name by parser.storing("--name", "-n", help = "Configuration name").default("Marathon tests")
    val outputDir by parser.storing("--output", "-o", help = "Output folder") { File(this) }.default("")
    val applicationOutput by parser.storing("--application", help = "Application file") { File(this) }.default("")
    val testApplicationOutput by parser.storing("--test-application", help = "Test application file") { File(this) }.default("")
    val androidSdkDir by parser.storing("--android-sdk", help = "Android sdk location") { File(this) }.default("")
}