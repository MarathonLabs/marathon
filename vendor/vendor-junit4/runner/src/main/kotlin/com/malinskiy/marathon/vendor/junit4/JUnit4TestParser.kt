package com.malinskiy.marathon.vendor.junit4

import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.vendor.junit4.configuration.Junit4Configuration
import com.malinskiy.marathon.vendor.junit4.parsing.JarParser

class JUnit4TestParser : TestParser {
    override fun extract(configuration: Configuration): List<Test> {
        val conf = configuration.vendorConfiguration as Junit4Configuration

        return JarParser().findTests(conf.testsJar)
    }
}
