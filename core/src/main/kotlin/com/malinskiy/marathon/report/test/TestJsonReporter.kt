package com.malinskiy.marathon.report.test

import com.google.gson.Gson
import com.malinskiy.marathon.analytics.internal.sub.ExecutionReport
import com.malinskiy.marathon.report.ProgressReporter
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FileType

internal class TestJsonReporter(
    private val fileManager: FileManager,
    private val gson: Gson
) : ProgressReporter {
    override fun end(executionReport: ExecutionReport) {
        for (testEvent in executionReport.testEvents.filter { it.final }) {
            val file = fileManager.createFile(FileType.TEST_RESULT, testEvent.poolId, testEvent.device, testEvent.testResult.test)
            file.writeText(gson.toJson(testEvent.testResult))
        }
    }
}
