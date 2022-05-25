package com.malinskiy.marathon.android.executor.listeners.filesync

import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.InstrumentationInfo
import com.malinskiy.marathon.android.RemoteFileManager
import com.malinskiy.marathon.android.executor.listeners.AndroidTestRunListener
import com.malinskiy.marathon.config.vendor.android.AggregationMode
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
            val remoteResourcesFolderPath = RemoteFileManager.TMP_PATH.plus("/resources")
            device.fileManager.removeRemotePath(remoteResourcesFolderPath, recursive = true)
            device.fileManager.createRemoteDirectory(remoteResourcesFolderPath)
            configuration.push.forEach { entry ->
                val entryFile = File(entry.relativePath)
                if (entryFile.isDirectory) {
                    entryFile.walkTopDown().forEach { file ->
                        if (!file.isDirectory) {
                            val remotePath = "${remoteResourcesFolderPath}/${file.path.replace(Regex(".*${entryFile.name}/"), "")}"
                            logger.debug { "Pushing file $file into $remotePath" }
                            device.pushFile(file.path, remotePath, true)
                        }
                    }
                } else {
                    val remotePath = remoteResourcesFolderPath.plus("/${entryFile.name}")
                    logger.debug { "Pushing file $entryFile into $remotePath" }
                    device.pushFile(entryFile.path, remotePath, true)
                }
            }
        }
    }

    private fun getExternalFolderPath(device: AndroidDevice, relativePath: String): String {
        val externalStorageMount = device.externalStorageMount.removeSuffix("/")
        val relativePath = relativePath.removePrefix("/")
        return "$externalStorageMount/$relativePath"
    }

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
                PathRoot.APP_DATA -> {
                    applicationPackage?.let {
                        val (from, to) = getScopedFolderPaths(entry.relativePath, RemoteFileManager.TMP_PATH, it)
                        moveScopedFolder(from, to, it)
                        device.safePullFolder(to, subfolder.absolutePath)
                    } ?: logger.warn { "Unable to pull $entry: application package is unknown" }
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
        device.safeExecuteShellCommand("run-as $pkg sh -c 'cd $from && tar cf - .' | tar xvf - -C $to && run-as $pkg rm -R $from")
    }
}
