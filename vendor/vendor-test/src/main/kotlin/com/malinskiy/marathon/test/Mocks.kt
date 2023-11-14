package com.malinskiy.marathon.test

import org.mockito.kotlin.mock

class Mocks {
    class TestParser {
        companion object {
            val DEFAULT = mock<com.malinskiy.marathon.execution.LocalTestParser>()
        }
    }
}
