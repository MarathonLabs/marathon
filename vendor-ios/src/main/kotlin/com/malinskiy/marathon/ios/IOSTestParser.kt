package com.malinskiy.marathon.ios

import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toTestName
import mu.KotlinLogging
import java.io.File
import java.io.FileNotFoundException

private val logger = KotlinLogging.logger { }

class IOSTestParser : TestParser {

    override fun extract(configuration: Configuration): List<Test> {
        return emptyList()
    }
}
