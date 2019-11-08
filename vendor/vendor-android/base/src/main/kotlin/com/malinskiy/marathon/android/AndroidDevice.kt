package com.malinskiy.marathon.android

import com.android.sdklib.AndroidVersion
import com.malinskiy.marathon.android.executor.listeners.line.LineListener
import com.malinskiy.marathon.android.executor.listeners.video.ScreenRecorderOptions
import com.malinskiy.marathon.device.Device
import java.awt.image.BufferedImage
import java.util.concurrent.TimeUnit

interface AndroidDevice : Device {
    val apiLevel: Int
    val version: AndroidVersion

    val fileManager: RemoteFileManager

    fun getExternalStorageMount(): String

    fun executeCommand(command: String, errorMessage: String)
    fun pullFile(remoteFilePath: String, localFilePath: String)
    fun safeUninstallPackage(appPackage: String): String?
    fun safeInstallPackage(absolutePath: String, reinstall: Boolean, optionalParams: String): String?
    fun safeExecuteShellCommand(command: String): String
    fun getScreenshot(timeout: Long, units: TimeUnit): BufferedImage

    fun addLogcatListener(listener: LineListener)
    fun removeLogcatListener(listener: LineListener)
    fun safeStartScreenRecorder(remoteFilePath: String, listener: LineListener, options: ScreenRecorderOptions)
}