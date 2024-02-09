package com.malinskiy.marathon.apple.ios.logparser

import com.malinskiy.marathon.apple.ios.test.TestEvent

interface TestEventProducer {
    fun process(line: String): List<TestEvent>?
}
