package com.malinskiy.marathon.execution

import com.malinskiy.marathon.test.Test

interface TestFilter {
    fun validate()

    fun filter(tests: List<Test>): List<Test>
    fun filterNot(tests: List<Test>): List<Test>
}
