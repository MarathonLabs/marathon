package com.malinskiy.marathon.apple.logparser

import com.malinskiy.marathon.apple.logparser.parser.DeviceFailureParser
import com.malinskiy.marathon.apple.logparser.parser.TestRunProgressParser
import com.malinskiy.marathon.apple.test.TestEvent
import com.malinskiy.marathon.time.Timer

/**
 * Currently doesn't provide any guarantee on the methods that should be called even once
 *
 * @param targetName target name (or blueprint name) is not printed by xcodebuild output, we have to augment the results to still have
 *  'target/class_name/test_name' structure mapped properly to marathon's package.class.method
 */
class XctestEventProducer(targetName: String, timer: Timer) : com.malinskiy.marathon.apple.logparser.TestEventProducer {
    private val failureParser = DeviceFailureParser()
    private val testRunListener = TestRunProgressParser(timer, targetName)

    override fun process(line: String): List<TestEvent>? {
        return failureParser.process(line) ?: testRunListener.process(line)
    }
}
