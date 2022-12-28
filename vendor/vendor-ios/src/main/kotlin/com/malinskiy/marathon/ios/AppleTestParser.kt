package com.malinskiy.marathon.ios

import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.exceptions.TestParsingException
import com.malinskiy.marathon.execution.RemoteTestParser
import com.malinskiy.marathon.execution.withRetry
import com.malinskiy.marathon.ios.model.AppleTestBundle
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import kotlinx.coroutines.CancellationException
import java.io.File

class AppleTestParser(
    private val configuration: Configuration,
    private val vendorConfiguration: VendorConfiguration.IOSConfiguration,
    private val testBundleIdentifier: AppleTestBundleIdentifier
) : RemoteTestParser<AppleDeviceProvider> {
    private val logger = MarathonLogging.logger(AppleTestParser::class.java.simpleName)

    override suspend fun extract(deviceProvider: DeviceProvider): List<Test> {
        val app = vendorConfiguration.bundle?.app ?: throw IllegalArgumentException("No application bundle provided")
        val xctest = vendorConfiguration.bundle?.xctest ?: throw IllegalArgumentException("No test bundle provided")
        val possibleTestBinaries = xctest.listFiles()?.filter { it.isFile && it.extension == "" }
            ?: throw ConfigurationException("missing test binaries in xctest folder at $xctest")
        val testBinary = when (possibleTestBinaries.size) {
            0 -> throw ConfigurationException("missing test binaries in xctest folder at $xctest")
            1 -> possibleTestBinaries[0]
            else -> {
                logger.warn { "Multiple test binaries present in xctest folder" }
                possibleTestBinaries.find { it.name == xctest.nameWithoutExtension } ?: possibleTestBinaries.first()
            }
        }

        return withRetry(10, 0) {
            val channel = deviceProvider.subscribe()

            try {
                for (update in channel) {
                    if (update is DeviceProvider.DeviceEvent.DeviceConnected) {
                        val device = update.device as? AppleSimulatorDevice ?: continue
                        return@withRetry parseTests(device, xctest, testBinary)
                    }
                }
                throw RuntimeException("failed to parse")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.debug(e) { "Remote parsing failed. Retrying" }
                throw e
            } finally {
                channel.close()
            }
        }
    }

    private suspend fun parseTests(
        device: AppleSimulatorDevice,
        xctest: File,
        testBinary: File
    ): List<Test> {

        logger.debug { "Found test binary $testBinary for xctest $xctest" }

        device.remoteFileManager.createRemoteDirectory()
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


        val testBundle = AppleTestBundle(vendorConfiguration.bundle?.application, xctest, testBinary)
        swiftTests.forEach { testBundleIdentifier.put(it, testBundle) }
        objectiveCTests.forEach { testBundleIdentifier.put(it, testBundle) }

        return swiftTests + objectiveCTests
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
