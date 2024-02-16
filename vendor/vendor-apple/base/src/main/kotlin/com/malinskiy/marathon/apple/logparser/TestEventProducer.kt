package com.malinskiy.marathon.apple.logparser

import com.malinskiy.marathon.apple.test.TestEvent

interface TestEventProducer {
    fun process(line: String): List<TestEvent>?
}
