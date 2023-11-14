package com.malinskiy.marathon.ios.model

import com.dd.plist.NSDictionary
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.execution.bundle.TestBundle
import com.malinskiy.marathon.ios.plist.PropertyList
import com.malinskiy.marathon.ios.plist.bundle.BundleInfo
import com.malinskiy.marathon.log.MarathonLogging
import java.io.File

class AppleTestBundle(
    val application: File?,
    val testApplication: File,
) : TestBundle() {
    private val logger = MarathonLogging.logger {}
    override val id: String
        get() = testApplication.absolutePath

    val applicationBundleInfo: BundleInfo? by lazy {
        application?.let {
            PropertyList.from<NSDictionary, BundleInfo>(
                File(
                    it,
                    "Info.plist"
                )
            )
        }
    }
    val appId =
        applicationBundleInfo?.identification?.bundleIdentifier ?: throw ConfigurationException("No bundle identifier specified in $application")

    val testBundleInfo: BundleInfo by lazy { PropertyList.from(File(testApplication, "Info.plist")) }
    val testBundleId = (testBundleInfo.naming.bundleName ?: testApplication.nameWithoutExtension).replace('-', '_')

    val testBinary: File by lazy {
        val possibleTestBinaries = testApplication.listFiles()?.filter { it.isFile && it.extension == "" }
            ?: throw ConfigurationException("missing test binaries in xctest folder at $testApplication")
        when (possibleTestBinaries.size) {
            0 -> throw ConfigurationException("missing test binaries in xctest folder at $testApplication")
            1 -> possibleTestBinaries[0]
            else -> {
                logger.warn { "Multiple test binaries present in xctest folder" }
                possibleTestBinaries.find { it.name == testApplication.nameWithoutExtension } ?: possibleTestBinaries.first()
            }
        }
    }
}
