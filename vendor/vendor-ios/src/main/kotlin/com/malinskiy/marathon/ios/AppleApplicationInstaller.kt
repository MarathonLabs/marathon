package com.malinskiy.marathon.ios

import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.ios.TestType
import com.malinskiy.marathon.exceptions.DeviceSetupException
import com.malinskiy.marathon.execution.withRetry
import com.malinskiy.marathon.ios.extensions.testBundle
import com.malinskiy.marathon.ios.model.Sdk
import com.malinskiy.marathon.ios.xctestrun.TestRootFactory
import com.malinskiy.marathon.log.MarathonLogging

class AppleApplicationInstaller(
    private val configuration: Configuration,
    private val vendorConfiguration: VendorConfiguration.IOSConfiguration,
    private val testBundleIdentifier: AppleTestBundleIdentifier
) {
    private val logger = MarathonLogging.logger {}

    suspend fun prepareInstallation(device: AppleSimulatorDevice, useXctestParser: Boolean = false) {
        val bundle = vendorConfiguration.testBundle() ?: throw ConfigurationException("no xctest found for configuration")

        val xctest = bundle.testApplication
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
        val remoteTestBinary = device.remoteFileManager.joinPath(remoteXctest, testBinary.name)
        val testType = getTestTypeFor(device, device.sdk, remoteTestBinary)
        TestRootFactory(device, vendorConfiguration).generate(testType, bundle, useXctestParser)
        grantPermissions(device)

        vendorConfiguration.bundle?.extraApplications?.forEach {
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

    private suspend fun grantPermissions(device: AppleSimulatorDevice) {
        val bundleId = vendorConfiguration.permissions.bundleId
        if (bundleId != null) {
            for (permission in vendorConfiguration.permissions.grant) {
                device.grant(permission, bundleId)
            }
        } else if (vendorConfiguration.permissions.grant.isNotEmpty()) {
            logger.warn { "Unable to grant permissions due to unknown bundle identifier" }
        }

    }

    private suspend fun getTestTypeFor(device: AppleSimulatorDevice, sdk: Sdk, remoteTestBinary: String): TestType {
        val app = vendorConfiguration.bundle?.application
        val detectedTestType = vendorConfiguration.bundle?.testType ?: detectTestType(device, remoteTestBinary)

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
    private suspend fun detectTestType(device: AppleSimulatorDevice, remoteTestBinary: String): TestType {
        val output = device.binaryEnvironment.nm.list(remoteTestBinary)
        return if (output.any { it.contains("XCUIApplication") }) {
            TestType.XCUITEST
        } else {
            TestType.XCTEST
        }
    }
}
