package com.malinskiy.marathon.android.executor

import com.android.ddmlib.IDevice
import com.android.ddmlib.InstallException
import com.malinskiy.marathon.android.ApkParser
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.withRetry
import mu.KotlinLogging
import java.io.File

class AndroidAppInstaller(private val configuration: Configuration) {


    private val logger = KotlinLogging.logger("AndroidAppInstaller")

    //TODO: move InstrumentationInfo to Configuration
    fun prepareInstallation(device: IDevice) {
        val applicationInfo = ApkParser().parseInstrumentationInfo(configuration.testApplicationOutput)
        reinstall(device, applicationInfo.applicationPackage, configuration.applicationOutput)
        reinstall(device, applicationInfo.instrumentationPackage, configuration.testApplicationOutput)
    }

    private fun reinstall(device: IDevice, appPackage: String, appApk: File) {
        withRetry(3) {
            try {
                logger.info { "Uninstalling $appPackage from $device.serialNumber" }
                device.uninstallPackage(appPackage)
                logger.info { "Installing $appPackage to $device.serialNumber" }
                device.installPackage(appApk.absolutePath, true)
            } catch (e: InstallException) {
                throw RuntimeException("Error while installing $appPackage on ${device.serialNumber}", e)
            }
        }
    }
}