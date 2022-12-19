package com.malinskiy.marathon.ios.logparser

import com.malinskiy.marathon.ios.logparser.formatter.PackageNameFormatter
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureParser
import com.malinskiy.marathon.ios.logparser.parser.TestRunProgressParser
import com.malinskiy.marathon.ios.test.TestEvent
import com.malinskiy.marathon.time.Timer

/**
 * Currently doesn't provide any guarantee on the methods that should be called even once
 */
class XctestEventProducer(packageNameFormatter: PackageNameFormatter, timer: Timer) : TestEventProducer {
    private val failureParser = DeviceFailureParser()
    private val testRunListener = TestRunProgressParser(timer, packageNameFormatter)

    override fun process(line: String): List<TestEvent>? {
        return failureParser.process(line) ?: testRunListener.process(line)
    }
}
