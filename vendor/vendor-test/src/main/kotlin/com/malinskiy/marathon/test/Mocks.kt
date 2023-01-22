package com.malinskiy.marathon.test

import com.nhaarman.mockitokotlin2.mock

class Mocks {
    class TestParser {
        companion object {
            val DEFAULT = mock<com.malinskiy.marathon.execution.LocalTestParser>()
        }
    }
}
