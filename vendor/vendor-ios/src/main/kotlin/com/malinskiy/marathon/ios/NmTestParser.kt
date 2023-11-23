package com.malinskiy.marathon.ios

import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.ios.TestParserConfiguration
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.exceptions.TestParsingException
import com.malinskiy.marathon.execution.RemoteTestParser
import com.malinskiy.marathon.execution.withRetry
import com.malinskiy.marathon.ios.extensions.testBundle
import com.malinskiy.marathon.ios.model.AppleTestBundle
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import kotlinx.coroutines.CancellationException
import java.io.File

class NmTestParser(
    private val configuration: Configuration,
    private val vendorConfiguration: VendorConfiguration.IOSConfiguration,
    private val parserConfiguration: TestParserConfiguration.NmTestParserConfiguration,
    private val testBundleIdentifier: AppleTestBundleIdentifier
) : RemoteTestParser<AppleDeviceProvider> {
    private val logger = MarathonLogging.logger(NmTestParser::class.java.simpleName)

    override suspend fun extract(device: Device): List<Test> {
        val bundle = vendorConfiguration.testBundle()
        return withRetry(3, 0) {
            try {
                val device = device as? AppleSimulatorDevice ?: throw ConfigurationException("Unexpected device type for remote test parsing")
                return@withRetry parseTests(device, bundle)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.debug(e) { "Remote parsing failed. Retrying" }
                throw e
            }
        }
    }

    private suspend fun parseTests(
        device: AppleSimulatorDevice,
        bundle: AppleTestBundle,
    ): List<Test> {
        val testBinary = bundle.testBinary
        val xctest = bundle.testApplication

        logger.debug { "Found test binary $testBinary for xctest $xctest" }

        device.remoteFileManager.createRemoteDirectory()
        device.remoteFileManager.createRemoteSharedDirectory()
        val remoteXctest = device.remoteFileManager.remoteXctestFile()
        if (!device.pushFile(xctest, remoteXctest)) {
            throw TestParsingException("failed to push xctest for test parsing")
        }
        val remoteTestBinary = device.remoteFileManager.joinPath(remoteXctest, testBinary.name)

        val rawSwiftTests = device.binaryEnvironment.nm.swiftTests(remoteTestBinary)
        val swiftTests = rawSwiftTests.map { it.trim().split('.') }
            .filter { it.size == 3 && it[2].startsWith("test") } //Common prefix for all Swift test method names
            .mapNotNull {
                val (bundleId, clazz, methodSignature) = it
                if (methodSignature.contains("DISABLE")) {
                    null
                } else {
                    //Remove signature of test method
                    val method = methodSignature.substringBefore('(')
                    Test(bundleId, clazz, method, emptyList())
                }
            }
        val rawObjectiveCTests = device.binaryEnvironment.nm.objectiveCTests(remoteTestBinary)
        val objectiveCTests = rawObjectiveCTests.map { it.trim().split(' ') }
            .filter { it.size == 2 && it[1].startsWith("test") }
            .map {
                val (clazz, method) = it
                //TODO: check this will actually work without the bundle id
                Test("", clazz, method, emptyList())
            }


        val testBundle = AppleTestBundle(vendorConfiguration.bundle?.app, xctest)
        swiftTests.forEach { testBundleIdentifier.put(it, testBundle) }
        objectiveCTests.forEach { testBundleIdentifier.put(it, testBundle) }

        return (swiftTests + objectiveCTests).filter { test ->
            parserConfiguration.testClassRegexes.all { it.matches(test.clazz) }
        }
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
