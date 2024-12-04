package com.malinskiy.marathon.report.device

import com.google.gson.Gson
import com.malinskiy.marathon.analytics.internal.sub.ExecutionReport
import com.malinskiy.marathon.report.ProgressReporter
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FileType

internal class DeviceInfoJsonReporter(
    private val fileManager: FileManager,
    private val gson: Gson
) : ProgressReporter {
    override fun end(executionReport: ExecutionReport) {
        executionReport.deviceConnectedEvents
            .forEach { event ->
                val json = gson.toJson(event.device)
                fileManager.createFile(FileType.DEVICE_INFO, event.poolId, event.device).writeText(json)
            }
    }
}
