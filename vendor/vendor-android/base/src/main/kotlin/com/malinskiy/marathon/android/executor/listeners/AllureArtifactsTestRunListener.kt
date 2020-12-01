package com.malinskiy.marathon.android.executor.listeners

import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.configuration.AllureConfiguration
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FolderType

class AllureArtifactsTestRunListener(
    private val device: AndroidDevice,
    private val configuration: AllureConfiguration,
    private val fileManager: FileManager
) : NoOpTestRunListener() {
    override suspend fun testRunEnded(elapsedTime: Long, runMetrics: Map<String, String>) {
        super.testRunEnded(elapsedTime, runMetrics)
        device.pullFolder(configuration.resultsDirectory, fileManager.createFolder(FolderType.ALLURE_DEVICE_RESULTS).absolutePath)
    }
}
