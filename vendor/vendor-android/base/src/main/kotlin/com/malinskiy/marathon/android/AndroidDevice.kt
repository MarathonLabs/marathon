package com.malinskiy.marathon.android

import com.malinskiy.marathon.android.executor.listeners.LineListener
import com.malinskiy.marathon.device.Device
import java.awt.image.BufferedImage
import java.util.concurrent.TimeUnit

interface AndroidDevice: Device {
    val apiLevel: Int

    fun getExternalStorageMount(): String

    fun executeCommand(command: String, errorMessage: String)
    fun pullFile(remoteFilePath: String, localFilePath: String)
    fun safeUninstallPackage(appPackage: String): String?
    fun safeInstallPackage(absolutePath: String, reinstall: Boolean, optionalParams: String): String?
    fun safeExecuteShellCommand(command: String): String
    fun getScreenshot(timeout: Long, units: TimeUnit): BufferedImage

    fun addLogcatListener(listener: LineListener)
    fun removeLogcatListener(listener: LineListener)
}