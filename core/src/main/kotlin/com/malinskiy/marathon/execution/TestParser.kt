package com.malinskiy.marathon.execution

import com.malinskiy.marathon.test.Test

interface TestParser {
    fun extract(configuration: Configuration): List<Test>
}
