package com.malinskiy.marathon.test

import com.malinskiy.marathon.execution.ComponentInfo

class Mocks {
    class TestParser {
        companion object {
            val DEFAULT = VendorTestParser()
        }
    }

    class VendorTestParser : com.malinskiy.marathon.execution.TestParser {

        var tests: List<Test> = emptyList()

        override suspend fun extract(componentInfo: ComponentInfo): List<Test> {
            return tests
        }

    }
}