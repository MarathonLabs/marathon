package com.malinskiy.marathon.android

import com.android.ddmlib.InstallException
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.withRetry
import com.malinskiy.marathon.io.FileHasher
import com.malinskiy.marathon.log.MarathonLogging
import java.io.File
import java.time.Instant

class AndroidAppInstaller(
    private val fileHasher: FileHasher,
    private val track: Track,
    configuration: Configuration
) {

    companion object {
        private const val MAX_RETIRES = 3
        private const val MARSHMALLOW_VERSION_CODE = 23
        private const val MD5_HASH_SIZE = 32
        private const val PACKAGE_PREFIX = "package:"
    }

    private val logger = MarathonLogging.logger("AndroidAppInstaller")
    private val androidConfiguration = configuration.vendorConfiguration as AndroidConfiguration
    private val installedApps: MutableMap<String, MutableMap<String, String>> = hashMapOf()

    suspend fun ensureInstalled(device: AndroidDevice, componentInfo: AndroidComponentInfo) {
        val applicationInfo = ApkParser().parseInstrumentationInfo(componentInfo.testApplicationOutput)
        logger.debug { "Installing application output to ${device.serialNumber}" }
        componentInfo.applicationOutput?.let {
            ensureInstalled(device, applicationInfo.applicationPackage, it)
        }
        logger.debug { "Installing instrumentation package to ${device.serialNumber}" }
        ensureInstalled(device, applicationInfo.instrumentationPackage, componentInfo.testApplicationOutput)
        logger.debug { "Prepare installation finished for ${device.serialNumber}" }
    }

    @Suppress("TooGenericExceptionThrown")
    private suspend fun ensureInstalled(device: AndroidDevice, appPackage: String, appApk: File) {
        withRetry(attempts = MAX_RETIRES, delayTime = 1000) {
            try {
                val checkStarted = Instant.now()
                val fileHash = fileHasher.getHash(appApk)
                val isApkInstalled = isApkInstalled(device, appPackage, fileHash)
                track.installationCheck(device.serialNumber, checkStarted, Instant.now())

                if (isApkInstalled) {
                    logger.info("Skipping installation of $appPackage on ${device.serialNumber} - APK is already installed")
                } else {
                    logger.info("Installing $appPackage, ${appApk.absolutePath} to ${device.serialNumber}")
                    val installationStarted = Instant.now()
                    val installMessage = device.safeInstallPackage(appApk.absolutePath, true, optionalParams(device))
                    installMessage?.let { logger.debug { it } }
                    track.installation(device.serialNumber, installationStarted, Instant.now())
                    installedApps
                        .getOrPut(device.serialNumber) { hashMapOf() }
                        .put(appPackage, fileHash)
                }
            } catch (e: InstallException) {
                logger.error(e) { "Error while installing $appPackage, ${appApk.absolutePath} on ${device.serialNumber}" }
                throw RuntimeException("Error while installing $appPackage on ${device.serialNumber}", e)
            }
        }
    }

    private suspend fun isApkInstalled(device: AndroidDevice, appPackage: String, fileHash: String): Boolean {
        if (installedApps[device.serialNumber]?.get(appPackage) == fileHash) {
            return true
        }

        val hashOnDevice = getHashOnDevice(device, appPackage) ?: return false
        return hashOnDevice == fileHash
    }

    private fun getHashOnDevice(device: AndroidDevice, appPackage: String): String? {
        val apkPaths = device
            .safeExecuteShellCommand("pm path $appPackage")
            .lines()
            .map { it.removePrefix(PACKAGE_PREFIX) }
            .filter { it.isNotBlank() }

        if (apkPaths.isEmpty()) return null
        if (apkPaths.size > 1) {
            logger.warn { "Multiple packages of $appPackage installed on ${device.serialNumber}, skipping hash check" }
            return null
        }

        val apkPath = apkPaths.first()
        val md5Output = device.safeExecuteShellCommand("md5sum \"$apkPath\"")

        val hash = md5Output.substringBefore(" ")
        if (hash.length != MD5_HASH_SIZE) {
            logger.warn { "Error while calculating hash for $appPackage on ${device.serialNumber}: ${md5Output}, skipping hash check" }
            return null
        }

        return hash
    }

    private fun optionalParams(device: AndroidDevice): String {
        val options = if (device.apiLevel >= MARSHMALLOW_VERSION_CODE && androidConfiguration.autoGrantPermission) {
            "-g -r"
        } else {
            "-r"
        }

        return if (androidConfiguration.installOptions.isNotEmpty()) {
            "$options ${androidConfiguration.installOptions}"
        } else {
            options
        }
    }
}
