package com.malinskiy.marathon.report.bill

import com.google.gson.Gson
import com.malinskiy.marathon.analytics.internal.sub.ExecutionReport
import com.malinskiy.marathon.report.ProgressReporter
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FileType
import com.malinskiy.marathon.usageanalytics.Event
import com.malinskiy.marathon.usageanalytics.tracker.UsageTracker

internal class BillingReporter(
    private val fileManager: FileManager,
    private val gson: Gson,
    private val usageTracker: UsageTracker,
) : ProgressReporter {
    override fun end(executionReport: ExecutionReport) {
        executionReport.bills.forEach {
            val json = gson.toJson(it)
            fileManager.createFile(FileType.BILL, it.pool, device = it.device).writeText(json)
        }

        usageTracker.trackEvent(Event.Devices(executionReport.bills.size))
        usageTracker.trackEvent(Event.Executed(seconds = executionReport.bills.sumOf { it.duration.seconds },
                                               success = executionReport.result,
                                               flakinessSeconds = executionReport.flakiness.seconds,
                                               durationSeconds = executionReport.duration.seconds))
    }
}

