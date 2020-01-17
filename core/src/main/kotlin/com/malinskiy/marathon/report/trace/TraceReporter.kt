package com.malinskiy.marathon.report.trace

import com.malinskiy.marathon.analytics.internal.sub.CacheLoadEvent
import com.malinskiy.marathon.analytics.internal.sub.CacheStoreEvent
import com.malinskiy.marathon.analytics.internal.sub.DeviceConnectedEvent
import com.malinskiy.marathon.analytics.internal.sub.DevicePreparingEvent
import com.malinskiy.marathon.analytics.internal.sub.DeviceProviderPreparingEvent
import com.malinskiy.marathon.analytics.internal.sub.Event
import com.malinskiy.marathon.analytics.internal.sub.ExecutingBatchEvent
import com.malinskiy.marathon.analytics.internal.sub.ExecutionReport
import com.malinskiy.marathon.analytics.internal.sub.InstallationCheckEvent
import com.malinskiy.marathon.analytics.internal.sub.InstallationEvent
import com.malinskiy.marathon.analytics.internal.sub.TestEvent
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.Reporter
import com.malinskiy.marathon.report.trace.chrome.CompleteEvent
import com.malinskiy.marathon.report.trace.chrome.InstantEvent
import com.malinskiy.marathon.report.trace.chrome.TraceEvent
import com.malinskiy.marathon.report.trace.chrome.TraceReport
import com.malinskiy.marathon.report.trace.chrome.TraceReportClient
import com.malinskiy.marathon.test.toSimpleSafeTestName
import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit.MICROS


class TraceReporter(
    private val rootOutput: File
) : Reporter {

    private val traceReporter = TraceReportClient()

    override fun generate(executionReport: ExecutionReport) {

        val traceDir = File(rootOutput, "/trace")
        traceDir.mkdirs()

        val traceFile = File(traceDir, "timeline.trace")
        val report = createTraceReport(executionReport)
        traceReporter.writeTo(traceFile, report)
    }

    private fun createTraceReport(executionReport: ExecutionReport): TraceReport {
        val allEvents = executionReport.allEvents
        val minTime: Instant = allEvents.getMinTime()
        val traceEvents = executionReport
            .allEvents
            .mapNotNull { it.mapToTraceEvent(minTime) }
            .toList()

        return TraceReport(traceEvents)
    }

    private fun Event.mapToTraceEvent(minTime: Instant): TraceEvent? =
        when (this) {
            is DeviceConnectedEvent -> InstantEvent(
                timestampMicroseconds = MICROS.between(minTime, instant),
                processId = GLOBAL_PROCESS,
                threadId = device.serialNumber,
                eventName = "device_connected",
                scope = InstantEvent.SCOPE_THREAD,
                color = TraceEvent.COLOR_YELLOW
            )
            is DevicePreparingEvent -> CompleteEvent(
                timestampMicroseconds = MICROS.between(minTime, start),
                durationMicroseconds = MICROS.between(start, finish),
                processId = GLOBAL_PROCESS,
                threadId = serialNumber,
                eventName = "device_preparing"
            )
            is DeviceProviderPreparingEvent -> CompleteEvent(
                timestampMicroseconds = MICROS.between(minTime, start),
                durationMicroseconds = MICROS.between(start, finish),
                processId = GLOBAL_PROCESS,
                threadId = serialNumber,
                eventName = "device_provider_preparing"
            )
            is InstallationCheckEvent -> CompleteEvent(
                timestampMicroseconds = MICROS.between(minTime, start),
                durationMicroseconds = MICROS.between(start, finish),
                processId = GLOBAL_PROCESS,
                threadId = serialNumber,
                eventName = "installation_check"
            )
            is InstallationEvent -> CompleteEvent(
                timestampMicroseconds = MICROS.between(minTime, start),
                durationMicroseconds = MICROS.between(start, finish),
                processId = GLOBAL_PROCESS,
                threadId = serialNumber,
                eventName = "installation"
            )
            is ExecutingBatchEvent -> CompleteEvent(
                timestampMicroseconds = MICROS.between(minTime, start),
                durationMicroseconds = MICROS.between(start, finish),
                processId = GLOBAL_PROCESS,
                threadId = serialNumber,
                eventName = "executing_batch"
            )
            is CacheStoreEvent -> CompleteEvent(
                timestampMicroseconds = MICROS.between(minTime, start),
                durationMicroseconds = MICROS.between(start, finish),
                processId = GLOBAL_PROCESS,
                threadId = CACHES_THREAD,
                eventName = "cache_store",
                args = mapOf("test_name" to test.toSimpleSafeTestName())
            )
            is CacheLoadEvent -> CompleteEvent(
                timestampMicroseconds = MICROS.between(minTime, start),
                durationMicroseconds = MICROS.between(start, finish),
                processId = GLOBAL_PROCESS,
                threadId = CACHES_THREAD,
                eventName = "cache_load",
                args = mapOf("test_name" to test.toSimpleSafeTestName())
            )
            is TestEvent -> {
                val start = if (testResult.isFromCache) instant else Instant.ofEpochMilli(testResult.startTime)
                val finish = if (testResult.isFromCache) instant else Instant.ofEpochMilli(testResult.endTime)

                CompleteEvent(
                    timestampMicroseconds = MICROS.between(minTime, start),
                    durationMicroseconds = MICROS.between(start, finish),
                    processId = GLOBAL_PROCESS,
                    threadId = device.serialNumber,
                    eventName = "test",
                    color = if (testResult.isSuccess) TraceEvent.COLOR_GOOD else TraceEvent.COLOR_BAD,
                    args = mapOf(
                        "test_name" to testResult.test.toSimpleSafeTestName(),
                        "test_status" to testResult.status.toString(),
                        "is_from_cache" to testResult.isFromCache
                    )
                )
            }
        }

    private fun List<Event>.getMinTime(): Instant = map {
        when (it) {
            is DeviceConnectedEvent -> it.instant
            is DevicePreparingEvent -> it.start
            is DeviceProviderPreparingEvent -> it.start
            is InstallationCheckEvent -> it.start
            is InstallationEvent -> it.start
            is ExecutingBatchEvent -> it.start
            is CacheStoreEvent -> it.start
            is CacheLoadEvent -> it.start
            is TestEvent -> it.instant
        }
    }.min() ?: Instant.EPOCH

    private companion object {
        private const val GLOBAL_PROCESS = "global"
        private const val CACHES_THREAD = "caches"
    }
}
