package com.malinskiy.marathon.cli.args

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import java.io.File

class MarathonCliConfiguration(parser: ArgParser) {
    val marathonfile: File by parser.storing("--marathonfile", "-m", help = "marathonfile file path") { File(this) }.default(File("Marathonfile"))
    val androidSdkDir: File? by parser.storing("--android-sdk", help = "Android sdk location") { File(this) }.default<File?>(null)
}
