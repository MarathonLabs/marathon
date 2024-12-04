package com.malinskiy.marathon.analytics.internal.sub

import com.malinskiy.marathon.report.ProgressReporter
import java.time.Instant
import java.util.*

class ExecutionReportGenerator(private val progressReporter: ProgressReporter) : TrackerInternal {
    private val devicePreparingEvents: MutableList<DevicePreparingEvent> = Collections.synchronizedList(LinkedList())
    private val deviceConnectedEvents: MutableList<DeviceConnectedEvent> = Collections.synchronizedList(LinkedList())
    private val deviceDisconnectedEvents: MutableList<DeviceDisconnectedEvent> = Collections.synchronizedList(LinkedList())
    private val deviceProviderPreparingEvents: MutableList<DeviceProviderPreparingEvent> = Collections.synchronizedList(LinkedList())
    private val testEvents: MutableList<TestEvent> = Collections.synchronizedList(mutableListOf())
    private val defaultStart = Instant.now()

    override fun track(event: Event) {
        when (event) {
            is DeviceConnectedEvent -> deviceConnectedEvents.add(event)
            is DeviceDisconnectedEvent -> deviceDisconnectedEvents.add(event)
            is DevicePreparingEvent -> devicePreparingEvents.add(event)
            is DeviceProviderPreparingEvent -> deviceProviderPreparingEvents.add(event)
            is TestEvent -> testEvents.add(event)
        }
    }

    override fun close() {
        val report = ExecutionReport(
            deviceConnectedEvents.sortedBy { it.instant },
            deviceDisconnectedEvents.sortedBy { it.instant },
            devicePreparingEvents.sortedBy { it.start },
            deviceProviderPreparingEvents.sortedBy { it.start },
            testEvents.sortedBy { it.instant },
            defaultStart
        )
        progressReporter.end(report)
    }
}
