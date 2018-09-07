package com.malinskiy.marathon.ios

import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toTestName
import mu.KotlinLogging
import java.io.File
import java.io.FileNotFoundException

private val logger = KotlinLogging.logger { }

class IOSTestParser : TestParser {
    private val swiftTestClassRegex = """class ([^:\s]+)\s*:\s*XCTestCase""".toRegex()
    private val swiftTestMethodRegex = """^.*func\s+(test[^(\s]*)\s*\(.*$""".toRegex()

    /**
     *  Looks up test methods running a text search in swift files. Considers classes that explicitly inherit
     *  from `XCTestCase` and method names starting with `test`. Scans all swift files found under `sourceRoot`
     *  specified in Marathonfile. When not specified, starts in working directory. Result excludes any tests
     *  marked as skipped in `xctestrun` file.
     */
    override fun extract(configuration: Configuration): List<Test> {
        val vendorConfiguration = configuration.vendorConfiguration
        if (vendorConfiguration !is IOSConfiguration) {
            throw IllegalStateException("Expected IOS configuration")
        }
        if (!configuration.sourceRoot.isDirectory) {
            throw IllegalArgumentException("Expected a directory at $vendorConfiguration.sourceRoot")
        }

        val xctestrun = XCTestRun(vendorConfiguration.xctestrunPath)
        val moduleName = xctestrun.moduleName

        val swiftFilesWithTests = configuration
                .sourceRoot
                .listFiles("swift")
                .filter(swiftTestClassRegex)

        val implementedTests = mutableListOf<Test>()
        for (file in swiftFilesWithTests) {
            var testClassName: String? = null
            for (line in file.readLines()) {
                val className = line.firstMatchOrNull(swiftTestClassRegex)
                val methodName = line.firstMatchOrNull(swiftTestMethodRegex)

                if (className != null) { testClassName = className }

                if (testClassName != null && methodName != null) {
                    implementedTests.add(Test(moduleName, testClassName, methodName, emptyList()))
                }
            }
        }

        val filteredTests = implementedTests.filter { !xctestrun.isSkipped(it) }

        logger.debug { filteredTests.map { "${it.clazz}.${it.method}" }.joinToString() }
        logger.info { "Found $filteredTests.size tests in $swiftFilesWithTests.size files"}

        return filteredTests
    }
}

private fun Sequence<File>.filter(contentsRegex: Regex): Sequence<File> {
    return filter { it.contains(contentsRegex) }
}

private fun File.listFiles(extension: String): Sequence<File> {
    return walkTopDown().filter { it.extension == extension }
}

private val MatchResult.firstGroup: String?
    get() { return groupValues.get(1) }

private fun String.firstMatchOrNull(regex: Regex): String? {
    return regex.find(this)?.firstGroup
}

private fun File.contains(contentsRegex: Regex): Boolean {
    return inputStream().bufferedReader().lineSequence().any { it.contains(contentsRegex) }
}
