package com.malinskiy.marathon.ios.xctestrun

import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.ios.TestType
import com.malinskiy.marathon.exceptions.DeviceSetupException
import com.malinskiy.marathon.exceptions.TransferException
import com.malinskiy.marathon.ios.AppleSimulatorDevice
import com.malinskiy.marathon.ios.RemoteFileManager
import com.malinskiy.marathon.ios.model.AppleTestBundle
import com.malinskiy.marathon.ios.model.Arch
import com.malinskiy.marathon.ios.model.Sdk
import com.malinskiy.marathon.ios.xctestrun.v2.Metadata
import com.malinskiy.marathon.ios.xctestrun.v2.TestConfiguration
import com.malinskiy.marathon.ios.xctestrun.v2.TestTarget
import com.malinskiy.marathon.ios.xctestrun.v2.Xctestrun
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createTempFile

class TestRootFactory(
    private val device: AppleSimulatorDevice,
    private val vendorConfiguration: VendorConfiguration.IOSConfiguration,
) {
    suspend fun generate(testType: TestType, bundle: AppleTestBundle) {
        val remoteFileManager = device.remoteFileManager

        val testRoot = remoteFileManager.remoteTestRoot()
        remoteFileManager.createRemoteDirectory(testRoot)
        val xctestrun = when (testType) {
            TestType.XCUITEST -> generateXCUITest(testRoot, remoteFileManager, bundle)
            TestType.XCTEST -> generateXCTest(testRoot, remoteFileManager, bundle)
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
        bundle: AppleTestBundle,
    ): Xctestrun {
        val sdkPlatformPath = device.binaryEnvironment.xcrun.getSdkPlatformPath(device.sdk)
        val platformLibraryPath = remoteFileManager.joinPath(sdkPlatformPath, "Developer", "Library")

        val testRunnerApp = generateTestRunnerApp(testRoot, platformLibraryPath, bundle)
        val testApp = bundle.application ?: throw ConfigurationException("no application specified for XCUITest")

        val remoteTestApp = device.remoteFileManager.remoteApplication()
        if (!device.pushFolder(testApp, remoteTestApp)) {
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

        val userFrameworkPath =
            vendorConfiguration.xctestrunEnv["DYLD_FRAMEWORK_PATH"]?.split(":")?.filter { it.isNotBlank() } ?: emptySet()
        val userLibraryPath = vendorConfiguration.xctestrunEnv["DYLD_LIBRARY_PATH"]?.split(":")?.filter { it.isNotBlank() } ?: emptySet()

        val dyldFrameworks = mutableListOf("__TESTROOT__", frameworks, privateFrameworks, *userFrameworkPath.toTypedArray())
        val dyldLibraries = listOf("__TESTROOT__", usrLib, *userLibraryPath.toTypedArray())

        /**
         * If the app contains internal frameworks we need to add them to xctestrun
         */
        if (File(testApp, "Frameworks").exists()) {
            dyldFrameworks.add("__TESTROOT__/${remoteFileManager.appUnderTestFileName()}/Frameworks")
        }

        val testEnv = mutableMapOf(
            "DYLD_FRAMEWORK_PATH" to dyldFrameworks.joinToString(":"),
            "DYLD_LIBRARY_PATH" to dyldLibraries.joinToString(":")
        ).apply {
            vendorConfiguration.xctestrunEnv
                .filterKeys { !setOf("DYLD_FRAMEWORK_PATH", "DYLD_LIBRARY_PATH").contains(it) }
                .forEach {
                    put(it.key, it.value)
                }
        }.toMap()

        return Xctestrun(
            metadata = Metadata(2),
            testConfigurations = arrayOf(
                TestConfiguration(
                    "marathon",
                    arrayOf(
                        TestTarget.withArtifactReinstall(
                            name = bundle.testBundleId,
                            testingEnvironmentVariables = testEnv,
                            productModuleName = bundle.testBundleId,
                            systemAttachmentLifetime = vendorConfiguration.xcresult.attachments.systemAttachmentLifetime.value,
                            userAttachmentLifetime = vendorConfiguration.xcresult.attachments.userAttachmentLifetime.value,
                            dependentProductPaths = arrayOf(testRunnerApp, remoteXctest, remoteTestApp),
                            isUITestBundle = true,
                            testBundlePath = remoteXctest,
                            testHostPath = testRunnerApp,
                            uiTargetAppPath = remoteTestApp,
                        )
                    )
                )
            )
        )
    }

    private suspend fun generateXCTest(
        testRoot: String,
        remoteFileManager: RemoteFileManager,
        bundle: AppleTestBundle
    ): Xctestrun {
        val testApp = bundle.application ?: throw ConfigurationException("no application specified for XCTest")

        val remoteTestApp = device.remoteFileManager.remoteApplication()
        if (!device.pushFolder(testApp, remoteTestApp)) {
            throw DeviceSetupException("failed to push app under test to remote device")
        }

        /**
         * A common scenario is to place xctest for unit tests inside the app's PlugIns.
         * This is what Xcode does out of the box
         */
        val remoteXctest = joinPath(remoteTestApp, "PlugIns", bundle.testApplication.name)
        remoteFileManager.createRemoteDirectory(joinPath(remoteTestApp, "PlugIns"))
        if (bundle.testApplication == Path.of(testApp.path, "PlugIns")) {
            //We already pushed it above
        } else {
            if (!device.pushFolder(bundle.testApplication, remoteXctest)) {
                throw DeviceSetupException("failed to push xctest to remote device")
            }
        }

        if (device.sdk == Sdk.IPHONEOS) {
            TODO("generate phone provisioning")
        }

        val platformName = device.sdk.platformName
        val developerPath = remoteFileManager.joinPath("__PLATFORMS__", "$platformName.platform", "Developer")


        val frameworks = remoteFileManager.joinPath(developerPath, "Library", "Frameworks")
        val privateFrameworks = remoteFileManager.joinPath(developerPath, "Library", "PrivateFrameworks")
        val usrLib = remoteFileManager.joinPath(developerPath, "usr", "lib")

        val userFrameworkPath =
            vendorConfiguration.xctestrunEnv["DYLD_FRAMEWORK_PATH"]?.split(":")?.filter { it.isNotBlank() } ?: emptySet()
        val userLibraryPath = vendorConfiguration.xctestrunEnv["DYLD_LIBRARY_PATH"]?.split(":")?.filter { it.isNotBlank() } ?: emptySet()
        val userInsertLibraries =
            vendorConfiguration.xctestrunEnv["DYLD_INSERT_LIBRARIES"]?.split(":")?.filter { it.isNotBlank() } ?: emptySet()

        val dyldFrameworks = mutableListOf("__TESTROOT__", frameworks, privateFrameworks, *userFrameworkPath.toTypedArray())
        /**
         * If the app contains internal frameworks we need to add them to xctestrun
         */
        if (File(testApp, "Frameworks").exists()) {
            dyldFrameworks.add("__TESTROOT__/${remoteFileManager.appUnderTestFileName()}/Frameworks")
        }

        val dyldLibraries = listOf("__TESTROOT__", usrLib, *userLibraryPath.toTypedArray())
        val bundleInject = if (device.sdk == Sdk.IPHONEOS) {
            "__TESTHOST__/Frameworks/libXCTestBundleInject.dylib"
        } else {
            "$developerPath/usr/lib/libXCTestBundleInject.dylib"
        }
        val dyldInsertLibraries = listOf(bundleInject, *userInsertLibraries.toTypedArray())

        val testEnv = mutableMapOf(
            "DYLD_FRAMEWORK_PATH" to dyldFrameworks.joinToString(":"),
            "DYLD_INSERT_LIBRARIES" to dyldInsertLibraries.joinToString(":"),
            "DYLD_LIBRARY_PATH" to dyldLibraries.joinToString(":"),
            "XCInjectBundleInto" to "__TESTHOST__/${remoteFileManager.appUnderTestFileName()}"
        ).apply {
            vendorConfiguration.xctestrunEnv
                .filterKeys { !setOf("DYLD_FRAMEWORK_PATH", "DYLD_INSERT_LIBRARIES", "DYLD_LIBRARY_PATH").contains(it) }
                .forEach {
                    put(it.key, it.value)
                }
        }.toMap()

        return Xctestrun(
            metadata = Metadata(2),
            testConfigurations = arrayOf(
                TestConfiguration(
                    "marathon",
                    arrayOf(
                        TestTarget.withArtifactReinstall(
                            name = bundle.testBundleId,
                            testingEnvironmentVariables = testEnv,
                            productModuleName = bundle.testBundleId,
                            systemAttachmentLifetime = vendorConfiguration.xcresult.attachments.systemAttachmentLifetime.value,
                            userAttachmentLifetime = vendorConfiguration.xcresult.attachments.userAttachmentLifetime.value,
                            testBundlePath = remoteXctest,
                            testHostPath = remoteTestApp,
                            isAppHostedTestBundle = true,
                        )
                    )
                )
            )
        )
    }

    private suspend fun generateTestRunnerApp(
        testRoot: String,
        platformLibraryPath: String,
        bundle: AppleTestBundle,
    ): String {
        val testBinary = joinPath(device.remoteFileManager.remoteXctestFile(), bundle.testApplication.nameWithoutExtension)
        val baseApp = joinPath(platformLibraryPath, "Xcode", "Agents", "XCTRunner.app")
        val runnerBinaryName = "${bundle.testBundleId}-Runner"
        val testRunnerApp = joinPath(testRoot, "$runnerBinaryName.app")
        device.remoteFileManager.copy(baseApp, testRunnerApp)

        val baseTestRunnerBinary = joinPath(testRunnerApp, "XCTRunner")
        val testRunnerBinary = joinPath(testRunnerApp, runnerBinaryName)
        device.remoteFileManager.copy(baseTestRunnerBinary, testRunnerBinary)

        matchArchitectures(testBinary, testRunnerBinary)

        val plist = joinPath(testRunnerApp, "Info.plist")
        device.binaryEnvironment.plistBuddy.apply {
            set(plist, "CFBundleName", runnerBinaryName)
            set(plist, "CFBundleExecutable", runnerBinaryName)
            set(plist, "CFBundleIdentifier", "com.apple.test.$runnerBinaryName")
        }

        return testRunnerApp
    }

    private fun joinPath(base: String, vararg args: String) = device.remoteFileManager.joinPath(base, *args)

    private suspend fun matchArchitectures(testBinary: String, testRunnerBinary: String) {
        if (device.arch == Arch.arm64e) {
            val supportedArchs = device.binaryEnvironment.lipo.getArch(testBinary)
            if (!supportedArchs.contains(Arch.arm64e)) {
                // Launch as plain arm64 if arm64e is not supported
                device.binaryEnvironment.lipo.removeArch(testRunnerBinary, Arch.arm64e)
            }
        } else if (device.sdk == Sdk.IPHONESIMULATOR) {
            val supportedArchs = device.binaryEnvironment.lipo.getArch(testBinary)
            if (supportedArchs.contains(Arch.x86_64) && device.arch != Arch.arm64) {
                // Launch as plain x86_64 if test binary has been built for simulator and is targeting x86_64
                device.binaryEnvironment.lipo.removeArch(testRunnerBinary, Arch.arm64)
            }
        }
    }
}
