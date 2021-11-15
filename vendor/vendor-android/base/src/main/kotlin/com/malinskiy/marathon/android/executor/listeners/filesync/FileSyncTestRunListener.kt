package com.malinskiy.marathon.android.executor.listeners.filesync

import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.InstrumentationInfo
import com.malinskiy.marathon.android.RemoteFileManager
import com.malinskiy.marathon.android.executor.listeners.AndroidTestRunListener
import com.malinskiy.marathon.config.vendor.android.AggregationMode
import com.malinskiy.marathon.config.vendor.android.FileSyncConfiguration
import com.malinskiy.marathon.config.vendor.android.FileSyncEntry
import com.malinskiy.marathon.config.vendor.android.PathRoot
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FolderType
import com.malinskiy.marathon.log.MarathonLogging
import java.io.File

class FileSyncTestRunListener(
    private val pool: DevicePoolId,
    private val device: AndroidDevice,
    private val configuration: FileSyncConfiguration,
    private val fileManager: FileManager,
) : AndroidTestRunListener {

    private val logger = MarathonLogging.logger("FileSyncTestRunListener")
    private var applicationPackage: String? = null

    override suspend fun beforeTestRun(info: InstrumentationInfo?) {
        super.beforeTestRun(info)
        applicationPackage = info?.applicationPackage

        configuration.pull.forEach { entry ->
            val path = getRemotePullableFullPath(entry)
            if (path != null) {
                device.fileManager.removeRemotePath(path, recursive = true)
                device.fileManager.createRemoteDirectory(path)
            }
        }
    }

    override suspend fun afterTestRun() {
        super.afterTestRun()
        configuration.pull.forEach { entry ->
            val path = getRemotePullableFullPath(entry)
            if (path == null) {
                logger.warn { "Unable to pull $entry: application package is unknown" }
            } else {
                val localFolder = when (entry.aggregationMode) {
                    AggregationMode.DEVICE_AND_POOL -> fileManager.createFolder(FolderType.DEVICE_FILES, pool, device.toDeviceInfo())
                    AggregationMode.DEVICE -> fileManager.createFolder(FolderType.DEVICE_FILES, device.toDeviceInfo())
                    AggregationMode.POOL -> fileManager.createFolder(FolderType.DEVICE_FILES, pool)
                    AggregationMode.TEST_RUN -> fileManager.createFolder(FolderType.DEVICE_FILES)
                }

                val basename = entry.relativePath.removeSuffix("/").substringAfterLast('/')
                val subfolder = File(localFolder, basename).apply { mkdirs() }
                logger.debug { "Pulling into ${subfolder.absolutePath}" }
                device.safePullFolder(path, subfolder.absolutePath)
            }
        }
        applicationPackage = null
    }

    private suspend fun getRemotePullableFullPath(entry: FileSyncEntry): String? {
        return when (entry.pathRoot) {
            PathRoot.EXTERNAL_STORAGE -> {
                val externalStorageMount = device.externalStorageMount.removeSuffix("/")
                val relativePath = entry.relativePath.removePrefix("/")
                "$externalStorageMount/$relativePath"
            }
            PathRoot.APP_DATA -> {
                applicationPackage?.let {
                    moveScopedFolder(entry.relativePath, RemoteFileManager.TMP_PATH, it)
                }
            }
        }
    }

    /**
     * Depends on tar for folder moving
     */
    private suspend fun moveScopedFolder(src: String, dst: String, pkg: String): String {
        val source = src.removePrefix("/")
        val destination = "${dst.removeSuffix("/")}/$source"
        device.safeExecuteShellCommand("mkdir -p $destination")
        device.safeExecuteShellCommand("run-as $pkg sh -c 'cd /data/data/$pkg/$source && tar cf - .' | tar xvf - -C $destination && run-as $pkg rm -R /data/data/$pkg/$source")
        return destination
    }
}
