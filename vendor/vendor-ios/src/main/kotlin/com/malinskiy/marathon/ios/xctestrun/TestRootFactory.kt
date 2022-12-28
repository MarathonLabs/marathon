package com.malinskiy.marathon.ios.xctestrun

import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.ios.AppleTestBundleConfiguration
import com.malinskiy.marathon.config.vendor.ios.TestType
import com.malinskiy.marathon.exceptions.DeviceSetupException
import com.malinskiy.marathon.exceptions.TransferException
import com.malinskiy.marathon.ios.AppleSimulatorDevice
import com.malinskiy.marathon.ios.RemoteFileManager
import com.malinskiy.marathon.ios.model.Arch
import com.malinskiy.marathon.ios.model.Sdk
import com.malinskiy.marathon.ios.plist.PropertyList
import com.malinskiy.marathon.ios.xctest.Xctest
import com.malinskiy.marathon.ios.xctestrun.v2.Metadata
import com.malinskiy.marathon.ios.xctestrun.v2.TestConfiguration
import com.malinskiy.marathon.ios.xctestrun.v2.TestTarget
import com.malinskiy.marathon.ios.xctestrun.v2.Xctestrun
import java.io.File
import kotlin.io.path.createTempFile

class TestRootFactory(private val device: AppleSimulatorDevice, private val vendorConfiguration: VendorConfiguration.IOSConfiguration) {
    suspend fun generate(testType: TestType, bundleConfiguration: AppleTestBundleConfiguration) {
        val remoteFileManager = device.remoteFileManager

        val testRoot = remoteFileManager.remoteTestRoot()
        remoteFileManager.createRemoteDirectory(testRoot)
        val xctestrun = when (testType) {
            TestType.XCUITEST -> generateXCUITest(testRoot, remoteFileManager, bundleConfiguration)
            TestType.XCTEST -> TODO()
            TestType.LOGIC_TEST -> TODO()
        }

        val tempXctestrunfile = createTempFile(device.udid, "xctestrun").toFile()
        try {
            xctestrun.saveAsXML(tempXctestrunfile)
            val remoteXctestrun = remoteFileManager.remoteXctestrunFile()
            if (!device.pushFile(tempXctestrunfile, remoteXctestrun)) {
                throw TransferException("Unable to transfer generated xctestrun file to ${device.serialNumber}")
            }
        } finally {
            tempXctestrunfile.delete()
        }
    }

    private suspend fun generateXCUITest(
        testRoot: String,
        remoteFileManager: RemoteFileManager,
        bundleConfiguration: AppleTestBundleConfiguration
    ): Xctestrun {
        val sdkPlatformPath = device.binaryEnvironment.xcrun.getSdkPlatformPath(device.sdk)
        val platformLibraryPath = remoteFileManager.joinPath(sdkPlatformPath, "Developer", "Library")

        val xctest : Xctest = PropertyList.from(File(bundleConfiguration.xctest, "Info.plist"))
        val testBundleId = (xctest.bundleName ?: bundleConfiguration.xctest.nameWithoutExtension).replace('-','_')
        val testRunnerApp = generateTestRunnerApp(testRoot, testBundleId, platformLibraryPath, bundleConfiguration)
        val testApp = bundleConfiguration.app ?: throw ConfigurationException("no application specified for XCUITest")
        val remoteTestApp = device.remoteFileManager.remoteApplication()
        if(!device.pushFolder(testApp, remoteTestApp)) {
            throw DeviceSetupException("failed to push app under test to remote device")
        }
        
        val runnerPlugins = remoteFileManager.joinPath(testRunnerApp, "PlugIns")
        remoteFileManager.createRemoteDirectory(runnerPlugins)
        val remoteXctest = remoteFileManager.remoteXctestFile()
        remoteFileManager.copy(remoteXctest, runnerPlugins)

        if (device.sdk == Sdk.IPHONEOS) {
            TODO("generate phone provisioning")
        }

        val platformName = device.sdk.platformName
        val developerPath = remoteFileManager.joinPath("__PLATFORMS__", "$platformName.platform", "Developer")

        val frameworks = remoteFileManager.joinPath(developerPath, "Library", "Frameworks")
        val privateFrameworks = remoteFileManager.joinPath(developerPath, "Library", "PrivateFrameworks")
        val usrLib = remoteFileManager.joinPath(developerPath, "usr", "lib")
        val testEnv = mapOf(
            "DYLD_FRAMEWORK_PATH" to "__TESTROOT__:$frameworks:$privateFrameworks",
            "DYLD_LIBRARY_PATH" to "__TESTROOT__:$usrLib"
        )
        
        return Xctestrun(
            metadata = Metadata(2),
            testConfigurations = arrayOf(
                TestConfiguration(
                    "marathon",
                    arrayOf(
                        TestTarget(
                            name = testBundleId,
                            testBundlePath = remoteXctest,
                            testHostPath = testRunnerApp,
                            testingEnvironmentVariables = testEnv,
                            uiTargetAppPath = remoteTestApp,
                            productModuleName = testBundleId,
                            systemAttachmentLifetime = vendorConfiguration.xcresult.attachments.systemAttachmentLifetime.value,
                            userAttachmentLifetime = vendorConfiguration.xcresult.attachments.userAttachmentLifetime.value,
                            dependentProductPaths = arrayOf(testRunnerApp, remoteXctest, remoteTestApp),
                            isUITestBundle = true,
                        )
                    )
                )
            )
        )
    }

    private suspend fun generateTestRunnerApp(
        testRoot: String,
        testBundleId: String,
        platformLibraryPath: String,
        bundleConfiguration: AppleTestBundleConfiguration
    ): String {
        val testBinary =
            device.remoteFileManager.joinPath(device.remoteFileManager.remoteXctestFile(), bundleConfiguration.xctest.nameWithoutExtension)

        val baseApp = device.remoteFileManager.joinPath(platformLibraryPath, "Xcode", "Agents", "XCTRunner.app")
        val runnerBinaryName = "$testBundleId-Runner"
        val testRunnerApp = device.remoteFileManager.joinPath(testRoot, "$runnerBinaryName.app")
        device.remoteFileManager.copy(baseApp, testRunnerApp)

        val baseTestRunnerBinary = device.remoteFileManager.joinPath(testRunnerApp, "XCTRunner")
        val testRunnerBinary = device.remoteFileManager.joinPath(testRunnerApp, runnerBinaryName)
        device.remoteFileManager.copy(baseTestRunnerBinary, testRunnerBinary)

        matchArchitectures(testBinary, testRunnerBinary)

        val plist = device.remoteFileManager.joinPath(testRunnerApp, "Info.plist")
        device.binaryEnvironment.plistBuddy.set(plist, "CFBundleName", runnerBinaryName)
        device.binaryEnvironment.plistBuddy.set(plist, "CFBundleExecutable", runnerBinaryName)
        device.binaryEnvironment.plistBuddy.set(plist, "CFBundleIdentifier", "com.apple.test.$runnerBinaryName")

        return testRunnerApp
    }

    private suspend fun matchArchitectures(testBinary: String, testRunnerBinary: String) {
        if (device.arch == Arch.arm64e) {
            val supportedArchs = device.binaryEnvironment.lipo.getArch(testBinary)
            if (!supportedArchs.contains(Arch.arm64e)) {
                // Launch as plain arm64 if arm64e is not supported
                device.binaryEnvironment.lipo.removeArch(testRunnerBinary, Arch.arm64e)
            }
        } else if (device.sdk == Sdk.IPHONESIMULATOR) {
            val supportedArchs = device.binaryEnvironment.lipo.getArch(testBinary)
            if (supportedArchs.contains(Arch.x86_64)) {
                // Launch as plain x86_64 if test binary has been built for simulator and is targeting x86_64
                device.binaryEnvironment.lipo.removeArch(testRunnerBinary, Arch.arm64)
            }
        }
    }
}
