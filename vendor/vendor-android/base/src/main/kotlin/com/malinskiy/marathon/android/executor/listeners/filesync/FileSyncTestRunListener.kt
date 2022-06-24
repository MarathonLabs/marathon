package com.malinskiy.marathon.android.executor.listeners.filesync

import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.InstrumentationInfo
import com.malinskiy.marathon.android.RemoteFileManager
import com.malinskiy.marathon.android.executor.listeners.AndroidTestRunListener
import com.malinskiy.marathon.config.vendor.android.AggregationMode
import com.malinskiy.marathon.config.vendor.android.FilePushEntry
import com.malinskiy.marathon.config.vendor.android.FileSyncConfiguration
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
            when (entry.pathRoot) {
                PathRoot.EXTERNAL_STORAGE -> {
                    val path = getExternalFolderPath(device, entry.relativePath)
                    device.fileManager.removeRemotePath(path, recursive = true)
                    device.fileManager.createRemoteDirectory(path)
                }

                PathRoot.LOCAL_TMP -> {
                    val path = getTempFolderPath(entry.relativePath)
                    device.fileManager.removeRemotePath(path, recursive = true)
                    device.fileManager.createRemoteDirectory(path)
                }

                PathRoot.APP_DATA -> {
                    applicationPackage?.let {
                        val (from, to) = getScopedFolderPaths(entry.relativePath, RemoteFileManager.TMP_PATH, it)
                        device.fileManager.removeRemotePath(to, recursive = true)
                        device.fileManager.createRemoteDirectory(to)

                        /**
                         * Scoped storage requires run-as
                         */
                        device.safeExecuteShellCommand("run-as $applicationPackage rm -R $from")
                        device.safeExecuteShellCommand("run-as $applicationPackage mkdir $from")
                    }
                }
            }
        }

        if (configuration.push.isNotEmpty()) {
            configuration.push.forEach { entry ->
                val entryFile = File(entry.path)
                when (entry.pathRoot) {
                    PathRoot.EXTERNAL_STORAGE -> {
                        val remotePath = getExternalFolderPath(device, entryFile.name)
                        push(entry, entryFile, remotePath)
                    }

                    PathRoot.LOCAL_TMP -> {
                        val remotePath = getTempFolderPath(entryFile.name)
                        push(entry, entryFile, remotePath)
                    }

                    PathRoot.APP_DATA -> logger.error { "Pushing files to app storage is not supported" }
                }
            }
        }
    }

    private suspend fun push(entry: FilePushEntry, local: File, remotePath: String) {
        if (local.isDirectory) {
            logger.debug { "Pushing folder $local into $remotePath" }
            device.pushFolder(entry.path, remotePath)
        } else {
            val remotePath = remotePath
            logger.debug { "Pushing file $local into $remotePath" }
            device.pushFile(local.path, remotePath, true)
        }
    }

    private fun sanitizeRemotePath(base: String, relative: String) = "${base.removeSuffix("/")}/${relative.removePrefix("/")}"
    private fun getExternalFolderPath(device: AndroidDevice, relativePath: String) =
        sanitizeRemotePath(device.externalStorageMount, relativePath)

    private fun getTempFolderPath(relativePath: String) = sanitizeRemotePath(RemoteFileManager.TMP_PATH, relativePath)

    override suspend fun afterTestRun() {
        super.afterTestRun()
        configuration.pull.forEach { entry ->
            val localFolder = when (entry.aggregationMode) {
                AggregationMode.DEVICE_AND_POOL -> fileManager.createFolder(FolderType.DEVICE_FILES, pool, device.toDeviceInfo())
                AggregationMode.DEVICE -> fileManager.createFolder(FolderType.DEVICE_FILES, device.toDeviceInfo())
                AggregationMode.POOL -> fileManager.createFolder(FolderType.DEVICE_FILES, pool)
                AggregationMode.TEST_RUN -> fileManager.createFolder(FolderType.DEVICE_FILES)
            }

            val basename = entry.relativePath.removeSuffix("/").substringAfterLast('/')
            val subfolder = File(localFolder, basename).apply { mkdirs() }
            logger.debug { "Pulling into ${subfolder.absolutePath}" }
            when (entry.pathRoot) {
                PathRoot.EXTERNAL_STORAGE -> {
                    val path = getExternalFolderPath(device, entry.relativePath)
                    device.safePullFolder(path, subfolder.absolutePath)
                }

                PathRoot.LOCAL_TMP -> {
                    val path = getTempFolderPath(entry.relativePath)
                    device.safePullFolder(path, subfolder.absolutePath)
                }

                PathRoot.APP_DATA -> {
                    applicationPackage?.let {
                        val (from, to) = getScopedFolderPaths(entry.relativePath, RemoteFileManager.TMP_PATH, it)
                        moveScopedFolder(from, to, it)
                        device.safePullFolder(to, subfolder.absolutePath)
                    } ?: logger.warn { "Unable to pull $entry: application package is unknown" }
                }
            }
        }
        if (configuration.push.isNotEmpty()) {
            configuration.push.forEach { entry ->
                val entryFile = File(entry.path)
                when (entry.pathRoot) {
                    PathRoot.EXTERNAL_STORAGE -> device.fileManager.removeRemotePath(
                        getExternalFolderPath(device, entryFile.name),
                        recursive = true
                    )

                    PathRoot.LOCAL_TMP ->
                        device.fileManager.removeRemotePath(getTempFolderPath(entryFile.name), recursive = true)

                    PathRoot.APP_DATA -> Unit
                }
            }
        }

        applicationPackage = null
    }

    private fun getScopedFolderPaths(src: String, dst: String, pkg: String): Pair<String, String> {
        val source = src.removePrefix("/")
        val to = "${dst.removeSuffix("/")}/$source"
        val from = "/data/data/$pkg/$source"
        return Pair(from, to)
    }

    /**
     * Depends on tar for folder moving
     */
    private suspend fun moveScopedFolder(from: String, to: String, pkg: String) {
        device.safeExecuteShellCommand("mkdir -p $to && run-as $pkg sh -c 'cd $from && tar cf - .' | tar xvf - -C $to && run-as $pkg rm -R $from")
    }
}
