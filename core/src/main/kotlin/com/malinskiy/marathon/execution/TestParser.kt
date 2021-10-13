package com.malinskiy.marathon.execution

import com.malinskiy.marathon.test.Test

interface TestParser {
    fun extract(): List<Test>
}
