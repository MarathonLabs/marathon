package com.malinskiy.marathon.apple

import com.malinskiy.marathon.apple.extensions.bundleConfiguration
import com.malinskiy.marathon.apple.extensions.xcresultConfiguration
import com.malinskiy.marathon.apple.extensions.xctestrunEnv
import com.malinskiy.marathon.apple.model.AppleTestBundle
import com.malinskiy.marathon.apple.model.Sdk
import com.malinskiy.marathon.apple.xctestrun.TestRootFactory
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.apple.TestType
import com.malinskiy.marathon.exceptions.DeviceSetupException
import com.malinskiy.marathon.execution.withRetry
import com.malinskiy.marathon.log.MarathonLogging

open class AppleApplicationInstaller<in T: AppleDevice>(
    protected open val vendorConfiguration: VendorConfiguration,
) {
    private val logger = MarathonLogging.logger {}

    suspend fun prepareInstallation(device: AppleDevice, useXctestParser: Boolean = false) {
        val bundleConfiguration = vendorConfiguration.bundleConfiguration()
        val xctestrunEnv = vendorConfiguration.xctestrunEnv() ?: throw IllegalArgumentException("No xctestrunEnv provided")
        val xcresultConfiguration = vendorConfiguration.xcresultConfiguration() ?: throw IllegalArgumentException("No xcresult configuration provided")
        val xctest = bundleConfiguration?.xctest ?: throw IllegalArgumentException("No test bundle provided")
        val app = bundleConfiguration.app
        val bundle = AppleTestBundle(app, xctest, device.sdk)
        val relativeTestBinaryPath = bundle.relativeBinaryPath

        logger.debug { "Moving xctest to ${device.serialNumber}" }
        val remoteXctest = device.remoteFileManager.remoteXctestFile()
        withRetry(3, 1000L) {
            device.remoteFileManager.createRemoteDirectory()
            device.remoteFileManager.createRemoteSharedDirectory()
            if (!device.pushFolder(xctest, remoteXctest)) {
                throw DeviceSetupException("Error transferring $xctest to ${device.serialNumber}")
            }
        }
        logger.debug { "Generating test root for ${device.serialNumber}" }

        val testBinary = bundle.testBinary
        val remoteTestBinary = device.remoteFileManager.joinPath(remoteXctest, *relativeTestBinaryPath, testBinary.name)
        val testType = getTestTypeFor(device, device.sdk, remoteTestBinary)
        TestRootFactory(device, xctestrunEnv, xcresultConfiguration).generate(testType, bundle, useXctestParser)
        afterInstall(device as T)

        bundleConfiguration.extraApplications?.forEach {
            if (it.isDirectory && it.extension == "app") {
                logger.debug { "Installing extra application $it to ${device.serialNumber}" }
                val remoteExtraApplication = device.remoteFileManager.remoteExtraApplication(it.name)
                device.pushFolder(it, remoteExtraApplication)
                device.install(remoteExtraApplication)
            } else {
                logger.warn { "Extra application $it should be a directory with extension app" }
            }
        }
    }

    open suspend fun afterInstall(device: T) = Unit

    private suspend fun getTestTypeFor(device: AppleDevice, sdk: Sdk, remoteTestBinary: String): TestType {
        val app = vendorConfiguration.bundleConfiguration()?.application
        val detectedTestType = vendorConfiguration.bundleConfiguration()?.testType ?: detectTestType(device, remoteTestBinary)

        return when {
            detectedTestType == TestType.LOGIC_TEST && sdk == Sdk.IPHONEOS && app != null -> {
                logger.warn { "Overriding test type to XCTest. Reason: target sdk (iPhone) doesn't support running logic tests" }
                TestType.XCTEST
            }

            detectedTestType == TestType.LOGIC_TEST && sdk == Sdk.IPHONEOS && app == null -> {
                throw ConfigurationException("Logic tests should target only iPhone Simulator")
            }

            detectedTestType == TestType.XCTEST && sdk == Sdk.IPHONESIMULATOR && app == null -> {
                logger.warn { "Overriding test type to Logic Test. Reason: found xctest bundle without application and targeting iPhone Simulator" }
                TestType.LOGIC_TEST
            }

            detectedTestType != TestType.LOGIC_TEST && app == null -> {
                throw ConfigurationException("Application is required for test type $detectedTestType")
            }

            else -> detectedTestType
        }
    }

    /**
     * Detect presence of XCUIApplication in the test binary
     */
    private suspend fun detectTestType(device: AppleDevice, remoteTestBinary: String): TestType {
        val output = device.binaryEnvironment.nm.list(remoteTestBinary)
        return if (output.any { it.contains("XCUIApplication") }) {
            TestType.XCUITEST
        } else {
            TestType.XCTEST
        }
    }
}
