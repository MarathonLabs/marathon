package com.malinskiy.marathon.test

import org.amshove.kluent.mock

class Mocks {
    class TestParser {
        companion object {
            val DEFAULT = mock(com.malinskiy.marathon.execution.TestParser::class)
        }
    }
}