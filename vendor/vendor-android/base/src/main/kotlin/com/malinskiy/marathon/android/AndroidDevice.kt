package com.malinskiy.marathon.android

import com.android.sdklib.AndroidVersion
import com.malinskiy.marathon.android.executor.listeners.line.LineListener
import com.malinskiy.marathon.device.Device
import java.awt.image.BufferedImage
import java.util.concurrent.TimeUnit

interface AndroidDevice : Device {
    val apiLevel: Int
    val version: AndroidVersion
    val fileManager: RemoteFileManager
    val externalStorageMount: String

    /**
     * Called only once per device's lifetime
     */
    suspend fun setup()

    suspend fun getProperty(name: String, cached: Boolean = true): String?

    /**
     * @return null if command did not complete successfully, otherwise cmd output
     */
    suspend fun executeShellCommand(command: String, errorMessage: String = ""): String?

    /**
     * @return null if command did not complete successfully, otherwise cmd output
     */
    suspend fun safeExecuteShellCommand(command: String, errorMessage: String = ""): String?

    suspend fun pullFile(remoteFilePath: String, localFilePath: String)
    suspend fun pushFile(localFilePath: String, remoteFilePath: String, verify: Boolean)

    suspend fun pullFolder(remoteFolderPath: String, localFolderPath: String)

    suspend fun safeInstallPackage(absolutePath: String, reinstall: Boolean, optionalParams: String): String?
    suspend fun safeUninstallPackage(appPackage: String, keepData: Boolean = false): String?
    suspend fun safeClearPackage(packageName: String): String?

    suspend fun getScreenshot(timeout: Long, units: TimeUnit): BufferedImage
    suspend fun safeStartScreenRecorder(remoteFilePath: String, options: VideoConfiguration)

    fun addLogcatListener(listener: LineListener)
    fun removeLogcatListener(listener: LineListener)
}
