package com.malinskiy.marathon.apple.ios

import com.malinskiy.marathon.apple.ios.extensions.testBundle
import com.malinskiy.marathon.apple.ios.model.AppleTestBundle
import com.malinskiy.marathon.apple.ios.test.TestEvent
import com.malinskiy.marathon.apple.ios.test.TestRequest
import com.malinskiy.marathon.apple.ios.test.TestStarted
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.exceptions.TestParsingException
import com.malinskiy.marathon.execution.RemoteTestParser
import com.malinskiy.marathon.execution.listener.LineListener
import com.malinskiy.marathon.execution.withRetry
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlin.io.path.outputStream

class XCTestParser(
    private val configuration: Configuration,
    private val vendorConfiguration: VendorConfiguration.IOSConfiguration,
    private val testBundleIdentifier: AppleTestBundleIdentifier
) : RemoteTestParser<AppleDeviceProvider>, LineListener {
    private val logger = MarathonLogging.logger(XCTestParser::class.java.simpleName)

    override suspend fun extract(device: Device): List<Test> {
        return withRetry(3, 0) {
            try {
                val device =
                    device as? AppleSimulatorDevice ?: throw ConfigurationException("Unexpected device type for remote test parsing")
                return@withRetry parseTests(device, configuration, vendorConfiguration)
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
        configuration: Configuration,
        vendorConfiguration: VendorConfiguration.IOSConfiguration
    ): List<Test> {
        val appleApplicationInstaller = AppleApplicationInstaller(configuration, vendorConfiguration, testBundleIdentifier)
        appleApplicationInstaller.prepareInstallation(device, useXctestParser = true)

        val dylib = javaClass.getResourceAsStream("/libxctest-parser.dylib")
        val tempFile = kotlin.io.path.createTempFile().apply {
            outputStream().use {
                dylib.copyTo(it)
            }
        }.toFile()
        val remoteLibParseTests = device.remoteFileManager.remoteXctestParserFile()
        if (!device.pushFile(tempFile, remoteLibParseTests)) {
            throw TestParsingException("failed to push libparse-tests.dylib for test parsing")
        }

        val remoteXctestrunFile = device.remoteFileManager.remoteXctestrunFile()
        val remoteDir = device.remoteFileManager.parentOf(remoteXctestrunFile)

        logger.debug("Remote xctestrun = $remoteXctestrunFile")

        val runnerRequest = TestRequest(
            workdir = remoteDir,
            remoteXctestrun = remoteXctestrunFile,
            coverage = false,
        )
        var channel: ReceiveChannel<List<TestEvent>>? = null
        var tests = mutableSetOf<Test>()
        try {
            lineBuffer.setLength(0)
            device.addLineListener(this)
            val localChannel = device.executeTestRequest(runnerRequest)
            channel = localChannel
            for (events in localChannel) {
                for (event in events) {
                    when (event) {
                        is TestStarted -> {
                            //Target name is never printed via xcodebuild. We create it using the bundle id in com.malinskiy.marathon.ios.xctestrun.TestRootFactory
                            val testWithTargetName = event.id.copy(pkg = vendorConfiguration.testBundle().testBundleId)
                            tests.add(testWithTargetName)
                        }
                        else -> Unit
                    }
                }
            }

            logger.debug { "Execution finished" }
        } catch (e: CancellationException) {
            val errorMessage = "Test parsing got stuck. " +
                "You can increase the timeout in settings if it's too strict"
            logger.error(e) { errorMessage }
        } finally {
            channel?.cancel()
            device.removeLineListener(this)
        }

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

        if (tests.size == 0) {
            logger.warn { "XCTestParser failed to parse tests. xcodebuild output:" + System.lineSeparator() + "$lineBuffer" }
        }

        val testBundle = AppleTestBundle(vendorConfiguration.bundle?.app, xctest)
        val result = tests.toList()
        result.forEach { testBundleIdentifier.put(it, testBundle) }

        return result
    }

    private val lineBuffer = StringBuffer()

    override suspend fun onLine(line: String) {
        lineBuffer.appendLine(line)
    }
}
