package com.malinskiy.marathon.android.executor.listeners

import com.android.ddmlib.logcat.LogCatMessage
import com.android.ddmlib.logcat.LogCatReceiverTask
import com.android.ddmlib.testrunner.TestIdentifier
import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.toTest
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.report.logs.LogWriter
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

class LogCatListener(private val device: AndroidDevice,
                     private val devicePoolId: DevicePoolId,
                     private val logWriter: LogWriter) : NoOpTestRunListener() {
    private val receiver = LogCatReceiverTask(device.ddmsDevice)

    private val ref = AtomicReference<MutableList<LogCatMessage>>(mutableListOf())

    private val listener: (MutableList<LogCatMessage>) -> Unit = {
        ref.get().addAll(it)
    }

    override fun testRunStarted(runName: String, testCount: Int) {
        receiver.addLogCatListener(listener)
        thread(name = "LogCatLogger-$runName-${device.serialNumber}") {
            receiver.run()
        }
    }

    override fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {
        val messages = ref.getAndSet(mutableListOf())
        logWriter.saveLogs(test.toTest(), devicePoolId, device, messages.map {
            "${it.timestamp} ${it.pid}-${it.tid}/${it.appName} ${it.logLevel.priorityLetter}/${it.tag}: ${it.message}"
        })
    }

    override fun testRunEnded(elapsedTime: Long, runMetrics: Map<String, String>) {
        receiver.stop()
        receiver.removeLogCatListener(listener)
    }
}
