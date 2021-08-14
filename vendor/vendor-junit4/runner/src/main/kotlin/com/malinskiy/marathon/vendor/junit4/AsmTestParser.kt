package com.malinskiy.marathon.vendor.junit4

import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.vendor.junit4.configuration.Junit4Configuration
import com.malinskiy.marathon.vendor.junit4.parsing.DirectoryParser
import com.malinskiy.marathon.vendor.junit4.parsing.JarParser

class AsmTestParser : TestParser {
    override suspend fun extract(configuration: Configuration): List<Test> {
        val conf = configuration.vendorConfiguration as Junit4Configuration
        val directoryParser = DirectoryParser()
        val jarParser = JarParser()

        return conf.testClasspath?.filter { it.exists() }?.flatMap {
            when {
                it.extension == "jar" -> {
                    jarParser.findTests(it)
                }
                it.extension == "zip" -> {
                    TODO("Zip support is currently not implemented")
                }
                it.isDirectory -> {
                    directoryParser.findTests(it)
                }
                else -> {
                    throw RuntimeException("Only test jars are supported")
                }
            }
        } ?: emptyList()
    }
}
