package com.malinskiy.marathon

import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestParser
import java.util.*

class Marathon(val configuration: Configuration) {

    fun run(): Boolean {

        val loader = ServiceLoader.load(TestParser::class.java)
        val testParser = loader.first()

        val tests = testParser.extract(configuration.testApplicationOutput)
        tests.forEach {
            println(it)
        }

        return false
    }
}