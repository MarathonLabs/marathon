package com.malinskiy.marathon.apple.model

import com.dd.plist.NSDictionary
import com.malinskiy.marathon.apple.plist.PropertyList
import com.malinskiy.marathon.apple.plist.bundle.BundleInfo
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.execution.bundle.TestBundle
import com.malinskiy.marathon.log.MarathonLogging
import java.io.File
import java.nio.file.Paths

class AppleTestBundle(
    val application: File?,
    val testApplication: File?,
    val xctestBundle: File,
    val sdk: Sdk,
) : TestBundle() {
    private val logger = MarathonLogging.logger {}
    override val id: String
        get() = xctestBundle.absolutePath

    val applicationBundleInfo: BundleInfo? by lazy {
        application?.let {
            PropertyList.from<NSDictionary, BundleInfo>(
                when (sdk) {
                    Sdk.IPHONEOS, Sdk.IPHONESIMULATOR, Sdk.TV, Sdk.TV_SIMULATOR, Sdk.WATCH, Sdk.WATCH_SIMULATOR, Sdk.VISION, Sdk.VISION_SIMULATOR -> File(
                        it,
                        "Info.plist"
                    )

                    Sdk.MACOS -> Paths.get(it.absolutePath, "Contents", "Info.plist").toFile()
                }
            )
        }
    }
    val appId =
        applicationBundleInfo?.identification?.bundleIdentifier
            ?: throw ConfigurationException("No bundle identifier specified in $application")

    val testBundleInfo: BundleInfo by lazy {
        val file = when (sdk) {
            Sdk.IPHONEOS, Sdk.IPHONESIMULATOR, Sdk.TV, Sdk.TV_SIMULATOR, Sdk.WATCH, Sdk.WATCH_SIMULATOR, Sdk.VISION, Sdk.VISION_SIMULATOR -> File(
                xctestBundle,
                "Info.plist"
            )

            Sdk.MACOS -> Paths.get(xctestBundle.absolutePath, "Contents", "Info.plist").toFile()
        }
        PropertyList.from(file)
    }
    val testBundleId = (testBundleInfo.naming.bundleName ?: xctestBundle.nameWithoutExtension).replace("[- ]".toRegex(), "_")

    val testBinary: File by lazy {
        val possibleTestBinaries = when (sdk) {
            Sdk.IPHONEOS, Sdk.IPHONESIMULATOR, Sdk.TV, Sdk.TV_SIMULATOR, Sdk.WATCH, Sdk.WATCH_SIMULATOR, Sdk.VISION, Sdk.VISION_SIMULATOR -> xctestBundle.listFiles()
                ?.filter { it.isFile && it.extension == "" }
                ?: throw ConfigurationException("missing test binaries in xctest folder at $xctestBundle")

            Sdk.MACOS -> Paths.get(xctestBundle.absolutePath, *relativeBinaryPath).toFile().listFiles()
                ?.filter { it.isFile && it.extension == "" }
                ?: throw ConfigurationException("missing test binaries in xctest folder at $xctestBundle")
        }
        when (possibleTestBinaries.size) {
            0 -> throw ConfigurationException("missing test binaries in xctest folder at $xctestBundle")
            1 -> possibleTestBinaries[0]
            else -> {
                logger.warn { "Multiple test binaries present [${possibleTestBinaries.joinToString(",") { it.name }}] in xctest folder" }
                possibleTestBinaries.find { it.name == xctestBundle.nameWithoutExtension } ?: possibleTestBinaries.first()
            }
        }
    }

    val testRunnerBinary: File by lazy {
        if (testApplication == null) {
            throw ConfigurationException("no test application provided")
        }

        val possibleTestRunnerBinaries = when (sdk) {
            Sdk.IPHONEOS, Sdk.IPHONESIMULATOR, Sdk.TV, Sdk.TV_SIMULATOR, Sdk.WATCH, Sdk.WATCH_SIMULATOR, Sdk.VISION, Sdk.VISION_SIMULATOR -> testApplication.listFiles()
                ?.filter { it.isFile && it.extension == "" }
                ?: throw ConfigurationException("missing test binaries in test runner folder at $testApplication")

            Sdk.MACOS -> Paths.get(testApplication.absolutePath, *relativeBinaryPath).toFile().listFiles()
                ?.filter { it.isFile && it.extension == "" }
                ?: throw ConfigurationException("missing test binaries in test runner folder at $testApplication")
        }
        when (possibleTestRunnerBinaries.size) {
            0 -> throw ConfigurationException("missing test binaries in test runner folder at $testApplication")
            1 -> possibleTestRunnerBinaries[0]
            else -> {
                logger.warn { "Multiple test binaries present [${possibleTestRunnerBinaries.joinToString(",") { it.name }}] in test runner folder" }
                possibleTestRunnerBinaries.find { it.name == testApplication.nameWithoutExtension } ?: possibleTestRunnerBinaries.first()
            }
        }
    }

    val applicationBinary: File? by lazy {
        application?.let { application ->
            when (sdk) {
                Sdk.IPHONEOS, Sdk.IPHONESIMULATOR, Sdk.TV, Sdk.TV_SIMULATOR, Sdk.WATCH, Sdk.WATCH_SIMULATOR, Sdk.VISION, Sdk.VISION_SIMULATOR -> application.listFiles()
                    ?.filter { it.isFile && it.extension == "" }

                Sdk.MACOS -> Paths.get(application.absolutePath, *relativeBinaryPath).toFile().listFiles()
                    ?.filter { it.isFile && it.extension == "" }
            }?.let { possibleBinaries ->
                when (possibleBinaries.size) {
                    0 -> null
                    1 -> possibleBinaries[0]
                    else -> {
                        logger.warn { "Multiple application binaries present in app folder" }
                        possibleBinaries[0]
                    }
                }
            }
        }
    }

    /**
     * Path of the app and test binaries relative to the bundle's (app/xctest) folder
     */
    val relativeBinaryPath: Array<String> by lazy {
        when (sdk) {
            Sdk.IPHONEOS, Sdk.IPHONESIMULATOR, Sdk.TV, Sdk.TV_SIMULATOR, Sdk.WATCH, Sdk.WATCH_SIMULATOR, Sdk.VISION, Sdk.VISION_SIMULATOR -> emptyArray()
            Sdk.MACOS -> arrayOf("Contents", "MacOS")
        }
    }
}
