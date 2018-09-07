package com.malinskiy.marathon.execution

import com.malinskiy.marathon.test.Test
import java.io.File

interface TestParser {
    fun extract(configuration: Configuration) : List<Test>
}
