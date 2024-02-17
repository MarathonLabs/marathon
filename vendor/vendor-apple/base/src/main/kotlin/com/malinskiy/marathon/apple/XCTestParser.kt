package com.malinskiy.marathon.apple

import com.malinskiy.marathon.apple.extensions.bundleConfiguration
import com.malinskiy.marathon.apple.model.AppleTestBundle
import com.malinskiy.marathon.apple.test.TestEvent
import com.malinskiy.marathon.apple.test.TestRequest
import com.malinskiy.marathon.apple.test.TestStarted
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.exceptions.TestParsingException
import com.malinskiy.marathon.execution.RemoteTestParser
import com.malinskiy.marathon.execution.listener.LineListener
import com.malinskiy.marathon.execution.withRetry
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlin.io.path.outputStream

class XCTestParser<T: AppleDevice>(
    private val configuration: Configuration,
    private val vendorConfiguration: VendorConfiguration,
    private val testBundleIdentifier: AppleTestBundleIdentifier,
    private val applicationInstaller: AppleApplicationInstaller<T>,
) : RemoteTestParser<DeviceProvider>, LineListener {
    private val logger = MarathonLogging.logger(XCTestParser::class.java.simpleName)

    override suspend fun extract(device: Device): List<Test> {
        return withRetry(3, 0) {
            try {
                val device =
                    device as? T ?: throw ConfigurationException("Unexpected device type for remote test parsing")
                val bundleConfiguration = vendorConfiguration.bundleConfiguration()
                val xctest = bundleConfiguration?.xctest ?: throw IllegalArgumentException("No test bundle provided")
                val app = bundleConfiguration.app
                val bundle = AppleTestBundle(app, xctest, device.sdk)
                return@withRetry parseTests(device, bundle, applicationInstaller)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.debug(e) { "Remote parsing failed. Retrying" }
                throw e
            }
        }
    }

    private suspend fun parseTests(
        device: AppleDevice,
        bundle: AppleTestBundle,
        applicationInstaller: AppleApplicationInstaller<T>,
    ): List<Test> {
        applicationInstaller.prepareInstallation(device, useXctestParser = true)

        val platform = device.sdk.platformName
        val dylib = javaClass.getResourceAsStream("/libxctest-parser/$platform/libxctest-parser.dylib")
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
                            val testWithTargetName = event.id.copy(pkg = bundle.testBundleId)
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

        if (tests.size == 0) {
            logger.warn { "XCTestParser failed to parse tests. xcodebuild output:" + System.lineSeparator() + "$lineBuffer" }
        }

        val result = tests.toList()
        result.forEach { testBundleIdentifier.put(it, bundle) }

        return result
    }

    private val lineBuffer = StringBuffer()

    override suspend fun onLine(line: String) {
        lineBuffer.appendLine(line)
    }
}
