package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.test.Test

interface TestFilter {
    fun filter(tests: List<Test>): List<Test>
    fun filterNot(tests: List<Test>): List<Test>
}
