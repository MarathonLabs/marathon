package com.malinskiy.marathon.android.executor

import com.android.ddmlib.IDevice
import com.android.ddmlib.InstallException
import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.AndroidComponentInfo
import com.malinskiy.marathon.android.ApkParser
import com.malinskiy.marathon.android.executor.listeners.video.CollectingShellOutputReceiver
import com.malinskiy.marathon.android.safeExecuteShellCommand
import com.malinskiy.marathon.android.safeInstallPackage
import com.malinskiy.marathon.android.safeUninstallPackage
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.withRetry
import com.malinskiy.marathon.log.MarathonLogging
import java.io.File

class AndroidAppInstaller(configuration: Configuration) {

    companion object {
        private const val MAX_RETIRES = 3
        private const val MARSHMALLOW_VERSION_CODE = 23
    }

    private val logger = MarathonLogging.logger("AndroidAppInstaller")
    private val androidConfiguration = configuration.vendorConfiguration as AndroidConfiguration

    suspend fun ensureInstalled(device: AndroidDevice, componentInfo: AndroidComponentInfo) {
        // TODO: we need to skip the installation if we detected that the app is already installed

        val applicationInfo = ApkParser().parseInstrumentationInfo(componentInfo.testApplicationOutput)
        logger.debug { "Installing application output to ${device.serialNumber}" }
        componentInfo.applicationOutput?.let {
            reinstall(device, applicationInfo.applicationPackage, it)
        }
        logger.debug { "Installing instrumentation package to ${device.serialNumber}" }
        reinstall(device, applicationInfo.instrumentationPackage, componentInfo.testApplicationOutput)
        logger.debug { "Prepare installation finished for ${device.serialNumber}" }
    }

    @Suppress("TooGenericExceptionThrown")
    private suspend fun reinstall(device: AndroidDevice, appPackage: String, appApk: File) {
        val ddmsDevice = device.ddmsDevice

        withRetry(attempts = MAX_RETIRES, delayTime = 1000) {
            try {
                if (installed(ddmsDevice, appPackage)) {
                    logger.info("Uninstalling $appPackage from ${device.serialNumber}")
                    val uninstallMessage = ddmsDevice.safeUninstallPackage(appPackage)
                    uninstallMessage?.let { logger.debug { it } }
                }
                logger.info("Installing $appPackage, ${appApk.absolutePath} to ${device.serialNumber}")
                val installMessage = ddmsDevice.safeInstallPackage(appApk.absolutePath, true, optionalParams(ddmsDevice))
                installMessage?.let { logger.debug { it } }
            } catch (e: InstallException) {
                logger.error(e) { "Error while installing $appPackage, ${appApk.absolutePath} on ${device.serialNumber}" }
                throw RuntimeException("Error while installing $appPackage on ${device.serialNumber}", e)
            }
        }
    }

    private fun installed(ddmsDevice: IDevice, appPackage: String): Boolean {
        val receiver = CollectingShellOutputReceiver()
        ddmsDevice.safeExecuteShellCommand("pm list packages", receiver)
        val lines = receiver.output().lines()
        return lines.any { it == "package:$appPackage" }
    }

    private fun optionalParams(device: IDevice): String {
        val options = if (device.version.apiLevel >= MARSHMALLOW_VERSION_CODE && androidConfiguration.autoGrantPermission) {
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