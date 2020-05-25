package com.malinskiy.marathon.ios.idb

import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test

class IdbTestParser : TestParser {
    private val logger = MarathonLogging.logger("IdbTestParser")
    override fun extract(configuration: Configuration): List<Test> {
        logger.info("return hardcoded test")
        return listOf(Test("com.test", "MagicTests", "testMyMethod", emptyList()))
    }
}
