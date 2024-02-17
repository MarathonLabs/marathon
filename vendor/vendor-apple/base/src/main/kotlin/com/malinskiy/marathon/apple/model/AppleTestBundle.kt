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
    val testApplication: File,
    val sdk: Sdk,
) : TestBundle() {
    private val logger = MarathonLogging.logger {}
    override val id: String
        get() = testApplication.absolutePath

    val applicationBundleInfo: BundleInfo? by lazy {
        application?.let {
            PropertyList.from<NSDictionary, BundleInfo>(
                when (sdk) {
                    Sdk.IPHONEOS, Sdk.IPHONESIMULATOR -> File(it, "Info.plist")
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
            Sdk.IPHONEOS, Sdk.IPHONESIMULATOR -> File(testApplication, "Info.plist")
            Sdk.MACOS -> Paths.get(testApplication.absolutePath, "Contents", "Info.plist").toFile()
        }
        PropertyList.from(file)
    }
    val testBundleId = (testBundleInfo.naming.bundleName ?: testApplication.nameWithoutExtension).replace('-', '_')

    val testBinary: File by lazy {
        val possibleTestBinaries = when (sdk) {
            Sdk.IPHONEOS, Sdk.IPHONESIMULATOR -> testApplication.listFiles()?.filter { it.isFile && it.extension == "" }
                ?: throw ConfigurationException("missing test binaries in xctest folder at $testApplication")

            Sdk.MACOS -> Paths.get(testApplication.absolutePath, *relativeTestBinaryPath).toFile().listFiles()
                ?.filter { it.isFile && it.extension == "" }
                ?: throw ConfigurationException("missing test binaries in xctest folder at $testApplication")
        }
        when (possibleTestBinaries.size) {
            0 -> throw ConfigurationException("missing test binaries in xctest folder at $testApplication")
            1 -> possibleTestBinaries[0]
            else -> {
                logger.warn { "Multiple test binaries present in xctest folder" }
                possibleTestBinaries.find { it.name == testApplication.nameWithoutExtension } ?: possibleTestBinaries.first()
            }
        }
    }

    /**
     * Path of the test binary relative to the xctest folder
     */
    val relativeTestBinaryPath: Array<String> by lazy {
        when (sdk) {
            Sdk.IPHONEOS, Sdk.IPHONESIMULATOR -> emptyArray()
            Sdk.MACOS -> arrayOf("Contents", "MacOS")
        }
    }
}
