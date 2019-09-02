package com.malinskiy.marathon.android.executor.listeners

import com.android.ddmlib.logcat.LogCatMessage
import com.android.ddmlib.logcat.LogCatReceiverTask
import com.android.ddmlib.testrunner.TestIdentifier
import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.toTest
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.Attachment
import com.malinskiy.marathon.execution.AttachmentType
import com.malinskiy.marathon.report.attachment.AttachmentListener
import com.malinskiy.marathon.report.attachment.AttachmentProvider
import com.malinskiy.marathon.report.logs.LogWriter
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

class LogCatListener(
    private val device: AndroidDevice,
    private val devicePoolId: DevicePoolId,
    private val logWriter: LogWriter
) : NoOpTestRunListener(), AttachmentProvider {
    private val attachmentListeners = mutableListOf<AttachmentListener>()

    override fun registerListener(listener: AttachmentListener) {
        attachmentListeners.add(listener)
    }

    private val receiver = LogCatReceiverTask(device.ddmsDevice)

    private val ref = AtomicReference<MutableList<LogCatMessage>>(mutableListOf())
    private var thread: Thread? = null

    private val listener: (MutableList<LogCatMessage>) -> Unit = {
        ref.get().addAll(it)
    }

    override fun testRunStarted(runName: String, testCount: Int) {
        receiver.addLogCatListener(listener)
        thread = thread(name = "LogCatLogger-$runName-${device.serialNumber}") {
            receiver.run()
        }
    }

    override fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {
        val messages = ref.getAndSet(mutableListOf())
        val file = logWriter.saveLogs(test.toTest(), devicePoolId, device.toDeviceInfo(), messages.map {
            "${it.timestamp} ${it.pid}-${it.tid}/${it.appName} ${it.logLevel.priorityLetter}/${it.tag}: ${it.message}"
        })

        attachmentListeners.forEach { it.onAttachment(test.toTest(), Attachment(file, AttachmentType.LOG)) }
    }

    override fun testRunEnded(elapsedTime: Long, runMetrics: Map<String, String>) {
        receiver.stop()
        receiver.removeLogCatListener(listener)
        thread?.interrupt()
    }
}
