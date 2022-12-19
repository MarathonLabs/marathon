package com.malinskiy.marathon.ios.logparser

import com.malinskiy.marathon.ios.test.TestEvent

interface TestEventProducer {
    fun process(line: String): List<TestEvent>?
}
