package com.malinskiy.marathon.android

import com.android.sdklib.AndroidVersion
import com.malinskiy.marathon.android.executor.listeners.line.LineListener
import com.malinskiy.marathon.android.model.Rotation
import com.malinskiy.marathon.config.vendor.android.VideoConfiguration
import com.malinskiy.marathon.device.Device
import java.awt.image.BufferedImage
import java.time.Duration

interface AndroidDevice : Device {
    val apiLevel: Int
    val version: AndroidVersion
    val fileManager: RemoteFileManager
    val externalStorageMount: String
    val initialRotation: Rotation

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

    /**
     * @throws com.malinskiy.marathon.android.exception.CommandRejectedException in case the command fails
     */
    suspend fun criticalExecuteShellCommand(command: String, errorMessage: String = ""): String

    /**
     * @throws com.malinskiy.marathon.android.exception.TransferException
     */
    suspend fun pullFile(remoteFilePath: String, localFilePath: String)

    /**
     * Soft exception handling version of pullFile
     */
    suspend fun safePullFile(remoteFilePath: String, localFilePath: String)

    /**
     * @throws com.malinskiy.marathon.android.exception.TransferException
     */
    suspend fun pushFile(localFilePath: String, remoteFilePath: String, verify: Boolean)

    /**
     * @throws com.malinskiy.marathon.android.exception.TransferException
     */
    suspend fun pullFolder(remoteFolderPath: String, localFolderPath: String)
    suspend fun safePullFolder(remoteFolderPath: String, localFolderPath: String)
    suspend fun pushFolder(localFolderPath: String, remoteFolderPath: String)
    /**
     * @throws com.malinskiy.marathon.android.exception.InstallException in case of failure to push the apk
     */
    suspend fun installPackage(absolutePath: String, reinstall: Boolean, optionalParams: List<String>): String?
    suspend fun safeUninstallPackage(appPackage: String, keepData: Boolean = false): String?
    suspend fun safeClearPackage(packageName: String): String?

    /**
     * @return screenshot or null if there was a failure
     */
    suspend fun getScreenshot(timeout: Duration): BufferedImage?
    suspend fun safeStartScreenRecorder(remoteFilePath: String, options: VideoConfiguration)

    fun addLogcatListener(listener: LineListener)
    fun removeLogcatListener(listener: LineListener)
}
