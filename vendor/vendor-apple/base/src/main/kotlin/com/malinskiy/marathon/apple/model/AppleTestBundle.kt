package com.malinskiy.marathon.apple.model

import com.dd.plist.NSDictionary
import com.malinskiy.marathon.apple.plist.PropertyList
import com.malinskiy.marathon.apple.plist.bundle.BundleInfo
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.execution.bundle.TestBundle
import com.malinskiy.marathon.log.MarathonLogging
import java.io.File
import java.nio.file.Paths

private val File.isAppleBinary: Boolean
    get() {
        inputStream().use {
            if (length() < 4) return false

            val header = UByteArray(4)
            it.read(header.asByteArray())

            return header.contentEquals(AppleTestBundle.FAT_MAGIC) || header.contentEquals(AppleTestBundle.MH_MAGIC) || header.contentEquals(AppleTestBundle.MH_MAGIC_64) ||
                header.contentEquals(AppleTestBundle.FAT_CIGAM) || header.contentEquals(AppleTestBundle.MH_CIGAM) || header.contentEquals(AppleTestBundle.MH_CIGAM_64)
        }
    }

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
                ?.filter { it.isFile && it.extension == "" && it.isAppleBinary }
                ?: throw ConfigurationException("missing test binaries in xctest folder at $xctestBundle")

            Sdk.MACOS -> Paths.get(xctestBundle.absolutePath, *relativeBinaryPath).toFile().listFiles()
                ?.filter { it.isFile && it.extension == "" && it.isAppleBinary }
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
                ?.filter { it.isFile && it.extension == "" && it.isAppleBinary  }
                ?: throw ConfigurationException("missing test binaries in test runner folder at $testApplication")

            Sdk.MACOS -> Paths.get(testApplication.absolutePath, *relativeBinaryPath).toFile().listFiles()
                ?.filter { it.isFile && it.extension == "" && it.isAppleBinary  }
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
                    ?.filter { it.isFile && it.extension == "" && it.isAppleBinary  }

                Sdk.MACOS -> Paths.get(application.absolutePath, *relativeBinaryPath).toFile().listFiles()
                    ?.filter { it.isFile && it.extension == "" && it.isAppleBinary  }
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

    /**
     * See mach-o specification for these
     * https://opensource.apple.com/source/xnu/xnu-4570.71.2/EXTERNAL_HEADERS/mach-o/loader.h.auto.html
     */
    companion object {
        val FAT_MAGIC: UByteArray = ubyteArrayOf(0xca.toUByte(), 0xfe.toUByte(), 0xba.toUByte(), 0xbe.toUByte())
        val FAT_CIGAM = FAT_MAGIC.reversedArray()
        val MH_MAGIC: UByteArray = ubyteArrayOf(0xfe.toUByte(), 0xed.toUByte(), 0xfa.toUByte(), 0xce.toUByte())
        val MH_CIGAM = MH_MAGIC.reversedArray()
        val MH_MAGIC_64: UByteArray = ubyteArrayOf(0xfe.toUByte(), 0xed.toUByte(), 0xfa.toUByte(), 0xcf.toUByte())
        val MH_CIGAM_64 = MH_MAGIC_64.reversedArray()
    }
}
