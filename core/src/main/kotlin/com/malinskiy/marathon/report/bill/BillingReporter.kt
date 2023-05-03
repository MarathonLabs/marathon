package com.malinskiy.marathon.report.bill

import com.google.gson.Gson
import com.malinskiy.marathon.analytics.internal.sub.ExecutionReport
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FileType
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.Reporter
import com.malinskiy.marathon.usageanalytics.Event
import com.malinskiy.marathon.usageanalytics.tracker.UsageTracker
import java.time.Duration
import java.time.Instant

internal class BillingReporter(
    private val fileManager: FileManager,
    private val gson: Gson,
    private val usageTracker: UsageTracker,
) : Reporter {
    private val defaultStart = Instant.now()
    private val logger = MarathonLogging.logger {}
    override fun generate(executionReport: ExecutionReport) {
        val starts = mutableMapOf<String, Instant>()
        val devices = executionReport.deviceConnectedEvents.associateBy { it.device.serialNumber }.mapValues { it.value.device }
        val pools = executionReport.deviceConnectedEvents.associateBy { it.device.serialNumber }.mapValues { it.value.poolId }

        executionReport.devicePreparingEvents.forEach {
            if (starts.contains(it.serialNumber)) {
                //Only replace if event finish is before current one
                if (starts[it.serialNumber]?.isAfter(it.finish) == true) {
                    starts[it.serialNumber] = it.finish
                }
            } else {
                starts[it.serialNumber] = it.finish
            }
        }

        val ends = mutableMapOf<String, Instant>();
        executionReport.deviceDisconnectedEvents.forEach {
            if (ends.contains(it.device.serialNumber)) {
                //Only replace if event instant is after current one
                if (ends[it.device.serialNumber]?.isBefore(it.instant) == true) {
                    ends[it.device.serialNumber] = it.instant
                }
            } else {
                ends[it.device.serialNumber] = it.instant
            }
        }

        val defaultEnd = Instant.now()
        val serials = starts.keys + ends.keys
        val bills: List<DeviceBill> = serials.mapNotNull {
            val start = starts[it] ?: defaultStart
            val end = ends[it] ?: defaultEnd
            val info = devices[it]
            val pool = pools[it]
            if (info != null && pool != null) {
                DeviceBill(info, pool, start.toEpochMilli(), end.toEpochMilli(), Duration.between(start, end).toMillis())
            } else {
                logger.warn { "Failure to process device bill: missing timeline event" }
                null
            }
        }

        bills.forEach {
            val json = gson.toJson(it)
            fileManager.createFile(FileType.BILL, it.pool, it.device).writeText(json)
        }

        usageTracker.trackEvent(Event.Devices(bills.size))
        usageTracker.trackEvent(Event.Executed(seconds = bills.sumOf { it.duration } / 1000))
    }
}

data class DeviceBill(
    val device: DeviceInfo,
    val pool: DevicePoolId,
    val start: Long,
    val end: Long,
    val duration: Long,
)
