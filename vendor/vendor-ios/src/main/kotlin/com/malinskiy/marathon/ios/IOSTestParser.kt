package com.malinskiy.marathon.ios

import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.ios.xctestrun.TestBundleInfo
import com.malinskiy.marathon.ios.xctestrun.Xctestrun
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.html.relativePathTo
import com.malinskiy.marathon.test.Test
import java.io.File

class IOSTestParser : TestParser {
    private val swiftTestClassRegex = """class ([^:\s]+)\s*:\s*XCTestCase""".toRegex()
    private val swiftTestMethodRegex = """^.*func\s+(test[^(\s]*)\s*\(.*$""".toRegex()

    private val logger = MarathonLogging.logger(IOSTestParser::class.java.simpleName)

    /**
     *  Looks up test methods running a text search in swift files. Considers classes that explicitly inherit
     *  from `XCTestCase` and method names starting with `test`. Scans all swift files found under `sourceRoot`
     *  specified in Marathonfile. When not specified, starts in working directory. Result excludes any tests
     *  marked as skipped in `xctestrun` file.
     */
    override fun extract(configuration: Configuration): List<Test> {
        val vendorConfiguration = configuration.vendorConfiguration as? IOSConfiguration
                ?: throw IllegalStateException("Expected IOS configuration")

        val dockerImageName = vendorConfiguration.binaryParserDockerImageName
        return when {
            !dockerImageName.isNullOrEmpty() -> extractWithDockerCommand(vendorConfiguration)
            else -> extractFromSourceFiles(vendorConfiguration)
        }
    }

    private fun extractWithDockerCommand(vendorConfiguration: IOSConfiguration): List<Test> {
        val dockerImageName = vendorConfiguration.binaryParserDockerImageName
                ?: throw IllegalStateException("Expected a docker image name")

        val files = targetExecutables(vendorConfiguration.xctestrunPath)

        val extractor = BinaryTestParser(dockerImageName)

        val regex = """^(.*)\.([^.]+)\.([^.(]+)\(\)$""".toRegex()
        val compiledTests = extractor.listTests(files).map {
            val parts = regex.find(it) ?: throw IllegalStateException("Invalid test name ${it}")
            if (parts.groupValues.size != 4) {
                throw IllegalStateException("Invalid test name ${it}")
            }
            Test(parts.groupValues[1], parts.groupValues[2], parts.groupValues[3], emptyList())
        }

        val xctestrun = Xctestrun(vendorConfiguration.xctestrunPath)
        val filteredTests = compiledTests.filter { !xctestrun.isSkipped(it) }

        logger.trace { filteredTests.map { "${it.clazz}.${it.method}" }.joinToString() }
        logger.info { "Found ${filteredTests.size} tests in ${files.count()} files"}

        return filteredTests
    }

    private fun targetExecutables(xctestrunPath: File): List<File> {
        val xctestrun = Xctestrun(xctestrunPath)
        return xctestrun.targetNames.map {
            xctestrun.testHostBundlePath(it)?.let { testHostBundle ->
                val testHostBundlePath = xctestrunPath.resolveSibling(testHostBundle)
                bundleExecutable(testHostBundlePath)
            }
        }.filterNotNull()
    }

    private fun bundleExecutable(bundle: File): File? {
        return bundleInfoPath(bundle)?.let { infoPath ->
            TestBundleInfo(infoPath).CFBundleExecutable()?.let { executable ->
                infoPath.resolveSibling(executable)
            }
        }
    }

    private fun bundleInfoPath(bundle: File): File? = bundle.walkTopDown().maxDepth(1).firstOrNull { it.name == "Info.plist" }

    private fun extractFromSourceFiles(vendorConfiguration: IOSConfiguration): List<Test> {
        if (!vendorConfiguration.sourceRoot.isDirectory) {
            throw IllegalArgumentException("Expected a directory at $vendorConfiguration.sourceRoot")
        }

        val sourceRoots = if (vendorConfiguration.sourceRootsRegex != null) {
            vendorConfiguration.sourceRoot.walkTopDown().filter {
                it.isDirectory && vendorConfiguration.sourceRootsRegex.containsMatchIn(it.relativePathTo(vendorConfiguration.sourceRoot))
            }.toList()
        } else listOf(vendorConfiguration.sourceRoot)

        val xctestrun = Xctestrun(vendorConfiguration.xctestrunPath)
        val targetName = vendorConfiguration.sourceTargetName
                ?: xctestrun.targetNames.firstOrNull()
                ?: throw IllegalStateException("sourceTargetName is not specified and " +
                        "there are no named targets in the provided xctestrun file")

        val swiftFilesWithTests = sourceRoots.map { sourceRoot ->
            sourceRoot.listFiles("swift").filter(swiftTestClassRegex)
        }

        val implementedTests = mutableListOf<Test>()
        for (fileSet in swiftFilesWithTests) {
            for (file in fileSet) {
                var testClassName: String? = null
                for (line in file.readLines()) {
                    val className = line.firstMatchOrNull(swiftTestClassRegex)
                    val methodName = line.firstMatchOrNull(swiftTestMethodRegex)

                    if (className != null) {
                        testClassName = className
                    }

                    if (testClassName != null && methodName != null) {
                        implementedTests.add(Test(targetName, testClassName, methodName, emptyList()))
                    }
                }
            }
        }

        val filteredTests = implementedTests.filter { !xctestrun.isSkipped(it) }

        logger.trace { filteredTests.joinToString { "${it.clazz}.${it.method}" } }
        logger.info { "Found ${filteredTests.size} tests in ${swiftFilesWithTests.count()} files"}

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

private fun File.resolveAsSiblingOf(file: File): File = file.resolveSibling(this)
