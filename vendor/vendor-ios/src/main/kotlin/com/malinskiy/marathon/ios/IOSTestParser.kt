package com.malinskiy.marathon.ios

import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.ios.xctestrun.Xctestrun
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.MetaProperty
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.getCucumberishTags
import java.io.File

class IOSTestParser(private val vendorConfiguration: VendorConfiguration.IOSConfiguration) : TestParser {
    private val swiftTestClassRegex = """class ([^:\s]+)\s*:\s*XCTestCase""".toRegex()
    private val swiftTestMethodRegex = """^.*func\s+(test[^(\s]*)\s*\(.*$""".toRegex()
    private val swiftTagRegex = """@([^:\s]+)\w*""".toRegex()

    private val logger = MarathonLogging.logger(IOSTestParser::class.java.simpleName)
    private val CUCUMBERISH_TAGS = "cucumberishTags"

    /**
     *  Looks up test methods running a text search in swift files. Considers classes that explicitly inherit
     *  from `XCTestCase` and method names starting with `test`. Scans all swift files found under `sourceRoot`
     *  specified in Marathonfile. When not specified, starts in working directory. Result excludes any tests
     *  marked as skipped in `xctestrun` file.
     */
    override suspend fun extract(): List<Test> {
        if (!vendorConfiguration.sourceRoot.isDirectory) {
            throw IllegalArgumentException("Expected a directory at $vendorConfiguration.sourceRoot")
        }

        val xctestrun = Xctestrun(vendorConfiguration.safecxtestrunPath())
        val targetName = xctestrun.targetName

        val swiftFilesWithTests = vendorConfiguration
            .sourceRoot
            .listFiles("swift")
            .filter(swiftTestClassRegex)

        val implementedTests = mutableListOf<Test>()
        for (file in swiftFilesWithTests) {
            var testClassName: String? = null
            for ((index, line) in file.readLines().withIndex()) {
                val className = line.firstMatchOrNull(swiftTestClassRegex)
                val methodName = line.firstMatchOrNull(swiftTestMethodRegex)

                if (className != null) {
                    testClassName = className
                }

                if (testClassName != null && methodName != null) {
                    // Find & Update Tags here
                    val tagLine = file.readLines()[index - 1]
                    val tags = swiftTagRegex.findAll(tagLine).map { it.groupValues[1] }.toList()

                    if (tags.isEmpty()) {
                        implementedTests.add(Test(targetName, testClassName, methodName, emptyList()))
                    }
                    else {
                        val values = mapOf("tags" to tags)
                        val metaProperty = MetaProperty(name = CUCUMBERISH_TAGS, values = values)
                        implementedTests.add(Test(targetName, testClassName, methodName, arrayListOf(metaProperty)))
                    }
                }
            }
        }

        var filteredTests = implementedTests.filter { !xctestrun.isSkipped(it) }

        filteredTests = filterByXCTestRunnerTag(filteredTests)

        logger.trace { filteredTests.map { "${it.clazz}.${it.method}" }.joinToString() }
        logger.info { "Found ${filteredTests.size} tests in ${swiftFilesWithTests.count()} files" }

        return filteredTests
    }

    private fun filterByXCTestRunnerTag(tests: List<Test>): List<Test> {
        if (vendorConfiguration.xcTestRunnerTag.isNullOrEmpty()) {
            return tests
        }

        var filteredTests = tests
        val testRunnerTag = vendorConfiguration.xcTestRunnerTag ?: ""

        if (testRunnerTag.isNotEmpty()) {
            filteredTests = filteredTests.filter {
                it.getCucumberishTags().contains(testRunnerTag)
            }
        }
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
    get() {
        return groupValues.get(1)
    }

private fun String.firstMatchOrNull(regex: Regex): String? {
    return regex.find(this)?.firstGroup
}

private fun File.contains(contentsRegex: Regex): Boolean {
    return inputStream().bufferedReader().lineSequence().any { it.contains(contentsRegex) }
}
