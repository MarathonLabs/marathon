package com.malinskiy.marathon.apple.xctestrun

import com.malinskiy.marathon.apple.AppleDevice
import com.malinskiy.marathon.apple.RemoteFileManager
import com.malinskiy.marathon.apple.logparser.parser.DeviceFailureException
import com.malinskiy.marathon.apple.logparser.parser.DeviceFailureReason
import com.malinskiy.marathon.apple.model.AppleOperatingSystemVersion
import com.malinskiy.marathon.apple.model.AppleTestBundle
import com.malinskiy.marathon.apple.model.Arch
import com.malinskiy.marathon.apple.model.Sdk
import com.malinskiy.marathon.apple.plist.bundle.BundleInfo
import com.malinskiy.marathon.apple.xctestrun.v2.TestConfiguration
import com.malinskiy.marathon.apple.xctestrun.v2.TestTarget
import com.malinskiy.marathon.apple.xctestrun.v2.Xctestrun
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.vendor.apple.TestType
import com.malinskiy.marathon.config.vendor.apple.ios.XcresultConfiguration
import com.malinskiy.marathon.config.vendor.apple.ios.XctestrunEnvConfiguration
import com.malinskiy.marathon.exceptions.DeviceSetupException
import com.malinskiy.marathon.exceptions.IncompatibleDeviceException
import com.malinskiy.marathon.exceptions.TransferException
import com.malinskiy.marathon.extension.relativePathTo
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createTempFile
import com.malinskiy.marathon.apple.xctestrun.v2.Metadata as Metadata

class TestRootFactory(
    private val device: AppleDevice,
    private val xctestrunEnv: XctestrunEnvConfiguration,
    private val xcresultConfiguration: XcresultConfiguration,
) {
    suspend fun generate(testType: TestType, bundle: AppleTestBundle, useXctestParser: Boolean) {
        val remoteFileManager = device.remoteFileManager

        val testRoot = remoteFileManager.remoteTestRoot()
        remoteFileManager.createRemoteDirectory(testRoot)
        validateDeviceCompatibility(device, bundle)

        val xctestrun = when (testType) {
            TestType.XCUITEST -> generateXCUITest(testRoot, remoteFileManager, bundle, useXctestParser)
            TestType.XCTEST -> generateXCTest(testRoot, remoteFileManager, bundle, useXctestParser)
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

    private fun validateDeviceCompatibility(device: AppleDevice, bundle: AppleTestBundle) {
        bundle.applicationBundleInfo?.let { validateDeviceBundle(it, device) }
        validateDeviceBundle(bundle.testBundleInfo, device)
    }

    private fun validateDeviceBundle(bundle: BundleInfo, device: AppleDevice) {
        val bundleSupportedPlatforms = bundle.undocumented.bundleSupportedPlatforms
        val platform = device.sdk.platformName
        if (!bundleSupportedPlatforms.contains(platform)) {
            throw DeviceFailureException(
                reason = DeviceFailureReason.IncompatibleDevice,
                message = "Device ${device.serialNumber} with platform $platform " +
                    "is incompatible with bundle's supported platforms [${
                        bundleSupportedPlatforms.joinToString(
                            ","
                        )
                    }]"
            )
        }

        if (device.sdk == Sdk.MACOS) {
            val lsMinimumSystemVersion = bundle.operatingSystemVersion.lsMinimumSystemVersion
            if (lsMinimumSystemVersion != null) {
                val bundleMinVersion = AppleOperatingSystemVersion(lsMinimumSystemVersion)
                val deviceVersion = AppleOperatingSystemVersion(device.operatingSystem.version)
                if (deviceVersion < bundleMinVersion) {
                    throw IncompatibleDeviceException("Device ${device.serialNumber} with os version ${device.operatingSystem.version} is incompatible with bundle's minimum required version $lsMinimumSystemVersion")
                }
            }
        }
    }

    private suspend fun generateXCUITest(
        testRoot: String,
        remoteFileManager: RemoteFileManager,
        bundle: AppleTestBundle,
        useLibParseTests: Boolean
    ): Xctestrun {
        val sdkPlatformPath = device.binaryEnvironment.xcrun.getSdkPlatformPath(device.sdk)
        val platformLibraryPath = remoteFileManager.joinPath(sdkPlatformPath, "Developer", "Library")

        val (testRunnerApp, remoteXctest) = if (bundle.testApplication != null) {
            reuseTestRunnerApp(testRoot, bundle, bundle.testApplication)
        } else {
            val testRunnerApp = generateTestRunnerApp(testRoot, platformLibraryPath, bundle)

            val runnerPlugins = when (device.sdk) {
                Sdk.IPHONEOS, Sdk.IPHONESIMULATOR, Sdk.TV, Sdk.TV_SIMULATOR, Sdk.WATCH, Sdk.WATCH_SIMULATOR, Sdk.VISION, Sdk.VISION_SIMULATOR -> remoteFileManager.joinPath(
                    testRunnerApp,
                    "PlugIns"
                )

                Sdk.MACOS -> remoteFileManager.joinPath(testRunnerApp, "Contents", "PlugIns")
            }
            remoteFileManager.createRemoteDirectory(runnerPlugins)
            val remoteXctest = remoteFileManager.remoteXctestFile()
            remoteFileManager.copy(remoteXctest, runnerPlugins, override = false)

            Pair(testRunnerApp, remoteXctest)
        }

        val testApp = bundle.application ?: throw ConfigurationException("no application specified for XCUITest")

        val remoteTestApp = device.remoteFileManager.remoteApplication()
        if (!device.pushFolder(testApp, remoteTestApp)) {
            throw DeviceSetupException("failed to push app under test to remote device")
        }
        ensureApplicationBinaryIsExecutable(remoteFileManager, bundle)

        if (device.sdk == Sdk.IPHONEOS) {
            TODO("generate phone provisioning")
        }

        val platformName = device.sdk.platformName
        val developerPath = remoteFileManager.joinPath("__PLATFORMS__", "$platformName.platform", "Developer")

        val frameworks = remoteFileManager.joinPath(developerPath, "Library", "Frameworks")
        val privateFrameworks = remoteFileManager.joinPath(developerPath, "Library", "PrivateFrameworks")
        val usrLib = remoteFileManager.joinPath(developerPath, "usr", "lib")

        val xctestrunTestEnv = xctestrunEnv.testEnvs
        val userFrameworkPath =
            xctestrunTestEnv["DYLD_FRAMEWORK_PATH"]?.split(":")?.filter { it.isNotBlank() } ?: emptySet()
        val userLibraryPath = xctestrunTestEnv["DYLD_LIBRARY_PATH"]?.split(":")?.filter { it.isNotBlank() } ?: emptySet()
        val userInsertLibraries = xctestrunTestEnv["DYLD_INSERT_LIBRARIES"]?.split(":")?.filter { it.isNotBlank() } ?: emptySet()

        val dyldFrameworks = mutableListOf<String>().apply {
            add("__TESTROOT__")
            add(
                joinPath(
                    remoteFileManager.remoteApplication(),
                    *bundle.relativeRootPath,
                    "Frameworks"
                )
            )
            add(
                joinPath(
                    testRunnerApp,
                    *bundle.relativeRootPath,
                    "Frameworks"
                )
            )
            add(frameworks)
            add(privateFrameworks)
            addAll(userFrameworkPath)
        }

        val dyldLibraries = mutableListOf<String>().apply {
            add("__TESTROOT__")

            val dylibsInFrameworks =
                bundle.testApplication?.let { Path(it.absolutePath, *bundle.relativeFrameworksPath) }?.toFile()?.listFiles()?.any {
                    it.extension == "dylib"
                }
            if (dylibsInFrameworks == true) {
                add(
                    joinPath(
                        testRunnerApp,
                        *bundle.relativeRootPath,
                        "Frameworks"
                    )
                )
            }

            add(usrLib)
            addAll(userLibraryPath)
        }


        val dyldInsertLibraries = if (useLibParseTests) {
            listOf(remoteFileManager.remoteXctestParserFile(), *userInsertLibraries.toTypedArray())
        } else {
            listOf(*userInsertLibraries.toTypedArray())
        }

        val testEnv = mutableMapOf(
            "DYLD_FRAMEWORK_PATH" to dyldFrameworks.joinToString(":"),
            "DYLD_LIBRARY_PATH" to dyldLibraries.joinToString(":"),
            "DYLD_INSERT_LIBRARIES" to dyldInsertLibraries.joinToString(":")
        ).apply {
            xctestrunTestEnv
                .filterKeys { !setOf("DYLD_FRAMEWORK_PATH", "DYLD_LIBRARY_PATH", "DYLD_INSERT_LIBRARIES").contains(it) }
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
                            environmentVariables = xctestrunEnv.appEnvs.ifEmpty { null },
                            testingEnvironmentVariables = testEnv,
                            productModuleName = bundle.testBundleId,
                            systemAttachmentLifetime = xcresultConfiguration.attachments.systemAttachmentLifetime.value,
                            userAttachmentLifetime = xcresultConfiguration.attachments.userAttachmentLifetime.value,
                            dependentProductPaths = arrayOf(testRunnerApp, remoteXctest, remoteTestApp),
                            isUITestBundle = true,
                            testBundlePath = remoteXctest,
                            testHostPath = testRunnerApp,
                            uiTargetAppPath = remoteTestApp,
                            preferredScreenCaptureFormat = xcresultConfiguration.preferredScreenCaptureFormat?.xcodevalue(),
                        )
                    )
                )
            )
        )
    }

    private suspend fun generateXCTest(
        testRoot: String,
        remoteFileManager: RemoteFileManager,
        bundle: AppleTestBundle,
        useLibParseTests: Boolean
    ): Xctestrun {
        val testApp = bundle.application ?: throw ConfigurationException("no application specified for XCTest")

        val remoteTestApp = device.remoteFileManager.remoteApplication()
        if (!device.pushFolder(testApp, remoteTestApp)) {
            throw DeviceSetupException("failed to push app under test to remote device")
        }
        ensureApplicationBinaryIsExecutable(remoteFileManager, bundle)

        /**
         * A common scenario is to place xctest for unit tests inside the app's PlugIns.
         * This is what Xcode does out of the box
         */
        val remoteXctest = joinPath(remoteTestApp, "PlugIns", bundle.xctestBundle.name)
        remoteFileManager.createRemoteDirectory(joinPath(remoteTestApp, "PlugIns"))
        if (bundle.xctestBundle == Path.of(testApp.path, "PlugIns")) {
            //We already pushed it above
        } else {
            if (!device.pushFolder(bundle.xctestBundle, remoteXctest)) {
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

        val xctestrunTestEnvs = xctestrunEnv.testEnvs
        val userFrameworkPath =
            xctestrunTestEnvs["DYLD_FRAMEWORK_PATH"]?.split(":")?.filter { it.isNotBlank() } ?: emptySet()
        val userLibraryPath = xctestrunTestEnvs["DYLD_LIBRARY_PATH"]?.split(":")?.filter { it.isNotBlank() } ?: emptySet()
        val userInsertLibraries =
            xctestrunTestEnvs["DYLD_INSERT_LIBRARIES"]?.split(":")?.filter { it.isNotBlank() } ?: emptySet()

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
        val dyldInsertLibraries = if (useLibParseTests) {
            listOf(remoteFileManager.remoteXctestParserFile(), bundleInject, *userInsertLibraries.toTypedArray())
        } else {
            listOf(bundleInject, *userInsertLibraries.toTypedArray())
        }

        val testEnv = mutableMapOf(
            "DYLD_FRAMEWORK_PATH" to dyldFrameworks.joinToString(":"),
            "DYLD_INSERT_LIBRARIES" to dyldInsertLibraries.joinToString(":"),
            "DYLD_LIBRARY_PATH" to dyldLibraries.joinToString(":"),
            "XCInjectBundleInto" to "__TESTHOST__/${remoteFileManager.appUnderTestFileName()}"
        ).apply {
            xctestrunTestEnvs
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
                            environmentVariables = xctestrunEnv.appEnvs.ifEmpty { null },
                            testingEnvironmentVariables = testEnv,
                            productModuleName = bundle.testBundleId,
                            systemAttachmentLifetime = xcresultConfiguration.attachments.systemAttachmentLifetime.value,
                            userAttachmentLifetime = xcresultConfiguration.attachments.userAttachmentLifetime.value,
                            testBundlePath = remoteXctest,
                            testHostPath = remoteTestApp,
                            isAppHostedTestBundle = true,
                        )
                    )
                )
            )
        )
    }

    private suspend fun ensureApplicationBinaryIsExecutable(
        remoteFileManager: RemoteFileManager,
        bundle: AppleTestBundle,
    ) {
        bundle.applicationBinary?.let {
            val remoteApplicationBinary = joinPath(
                remoteFileManager.remoteApplication(),
                *bundle.relativeBinaryPath,
                it.name
            )
            device.binaryEnvironment.chmod.makeExecutable(remoteApplicationBinary)
        }
    }

    private suspend fun ensureTestRunnerApplicationBinaryIsExecutable(
        remoteFileManager: RemoteFileManager,
        bundle: AppleTestBundle,
    ) {
        if (bundle.testApplication != null) {
            bundle.testRunnerBinary.let {
                val remoteTestApplicationBinary = joinPath(
                    remoteFileManager.remoteTestRunnerApplication(),
                    *bundle.relativeBinaryPath,
                    it.name
                )
                device.binaryEnvironment.chmod.makeExecutable(remoteTestApplicationBinary)
            }
        }
    }

    private suspend fun generateTestRunnerApp(
        testRoot: String,
        platformLibraryPath: String,
        bundle: AppleTestBundle,
    ): String {
        val remoteTestBinary = joinPath(
            device.remoteFileManager.remoteXctestFile(),
            *bundle.relativeBinaryPath,
            bundle.testBinary.nameWithoutExtension
        )
        val baseApp = joinPath(platformLibraryPath, "Xcode", "Agents", "XCTRunner.app")
        val runnerBinaryName = "${bundle.testBundleId}-Runner"
        val testRunnerApp = joinPath(testRoot, "$runnerBinaryName.app")
        device.remoteFileManager.copy(baseApp, testRunnerApp)

        val baseTestRunnerBinary = joinPath(testRunnerApp, *bundle.relativeBinaryPath, "XCTRunner")
        val testRunnerBinary = joinPath(testRunnerApp, *bundle.relativeBinaryPath, runnerBinaryName)
        device.remoteFileManager.copy(baseTestRunnerBinary, testRunnerBinary)

        matchArchitectures(remoteTestBinary, testRunnerBinary)

        val plist = when (device.sdk) {
            Sdk.IPHONEOS, Sdk.IPHONESIMULATOR, Sdk.TV, Sdk.TV_SIMULATOR, Sdk.WATCH, Sdk.WATCH_SIMULATOR, Sdk.VISION, Sdk.VISION_SIMULATOR -> joinPath(
                testRunnerApp,
                "Info.plist"
            )

            Sdk.MACOS -> joinPath(testRunnerApp, "Contents", "Info.plist")
        }

        device.binaryEnvironment.plistBuddy.apply {
            set(plist, "CFBundleName", runnerBinaryName)
            set(plist, "CFBundleExecutable", runnerBinaryName)
            set(plist, "CFBundleIdentifier", "com.apple.test.$runnerBinaryName")
        }

        return testRunnerApp
    }

    private suspend fun reuseTestRunnerApp(
        testRoot: String,
        bundle: AppleTestBundle,
        testApplication: File, //For null safety
    ): Pair<String, String> {
        ensureTestRunnerApplicationBinaryIsExecutable(device.remoteFileManager, bundle)
        val sharedTestRunnerApp = device.remoteFileManager.remoteTestRunnerApplication()
        val runnerBinaryName = "${bundle.testBundleId}-Runner"
        val testRunnerApp = joinPath(testRoot, "$runnerBinaryName.app")
        device.remoteFileManager.symlink(sharedTestRunnerApp, testRunnerApp)

        val testRunnerBinary =
            device.remoteFileManager.joinPath(sharedTestRunnerApp, *bundle.relativeBinaryPath, bundle.testRunnerBinary.name)
        val relativePath = bundle.xctestBundle.relativePathTo(testApplication).split(File.separator)
        val remoteXctest = device.remoteFileManager.joinPath(testRunnerApp, *relativePath.toTypedArray())
        val remoteTestBinary = joinPath(
            remoteXctest,
            *bundle.relativeBinaryPath,
            bundle.testBinary.nameWithoutExtension
        )

        matchArchitectures(remoteTestBinary, testRunnerBinary)

        return Pair(testRunnerApp, remoteXctest)
    }


    private fun joinPath(base: String, vararg args: String) = device.remoteFileManager.joinPath(base, *args)

    private suspend fun matchArchitectures(testBinary: String, testRunnerBinary: String) {
        if (device.arch == Arch.arm64e) {
            val supportedArchs = device.binaryEnvironment.lipo.getArch(testBinary)
            val testRunnerArchs = device.binaryEnvironment.lipo.getArch(testRunnerBinary)
            if (!supportedArchs.contains(Arch.arm64e) && testRunnerArchs.contains(Arch.arm64e)) {
                // Launch as plain arm64 if arm64e is not supported
                device.binaryEnvironment.lipo.removeArch(testRunnerBinary, Arch.arm64e)
            }
        } else if (device.sdk == Sdk.IPHONESIMULATOR) {
            val supportedArchs = device.binaryEnvironment.lipo.getArch(testBinary)
            val testRunnerArchs = device.binaryEnvironment.lipo.getArch(testRunnerBinary)
            if (supportedArchs.contains(Arch.x86_64) && device.arch != Arch.arm64 && testRunnerArchs.contains(Arch.arm64)) {
                // Launch as plain x86_64 if test binary has been built for simulator and is targeting x86_64
                device.binaryEnvironment.lipo.removeArch(testRunnerBinary, Arch.arm64)
            } else if (supportedArchs.contains(Arch.x86_64) && !supportedArchs.contains(Arch.arm64) && testRunnerArchs.contains(Arch.arm64)) {
                // Launch as plain x86_64 if test binary supports only x86_64
                device.binaryEnvironment.lipo.removeArch(testRunnerBinary, Arch.arm64)
            }
        }
    }
}
