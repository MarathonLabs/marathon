package com.malinskiy.marathon.android.executor.listeners

import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.InstrumentationInfo
import com.malinskiy.marathon.android.executor.listeners.line.LineListener
import com.malinskiy.marathon.android.model.TestIdentifier
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.Attachment
import com.malinskiy.marathon.execution.AttachmentType
import com.malinskiy.marathon.report.attachment.AttachmentListener
import com.malinskiy.marathon.report.attachment.AttachmentProvider
import com.malinskiy.marathon.report.logs.LogWriter

class LogCatListener(
    private val device: AndroidDevice,
    private val devicePoolId: DevicePoolId,
    private val testBatchId: String,
    private val logWriter: LogWriter
) : NoOpTestRunListener(), AttachmentProvider, LineListener {
    private val attachmentListeners = mutableListOf<AttachmentListener>()

    override fun registerListener(listener: AttachmentListener) {
        attachmentListeners.add(listener)
    }

    private val stringBuffer = StringBuffer(4096)

    override fun onLine(line: String) {
        stringBuffer.appendLine(line)
    }

    override suspend fun beforeTestRun(info: InstrumentationInfo?) {
        device.addLogcatListener(this)
    }
    
    override suspend fun testStarted(test: TestIdentifier) {
        stringBuffer.reset()
    }

    override suspend fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {
        val log = stringBuffer.toString()
        stringBuffer.reset()
        if(log.isNotEmpty()) {
            val file = logWriter.saveLogs(test.toTest(), devicePoolId, testBatchId, device.toDeviceInfo(), listOf(log))
            attachmentListeners.forEach { it.onAttachment(test.toTest(), Attachment(file, AttachmentType.LOG)) }
        }
    }

    override suspend fun afterTestRun() {
        device.removeLogcatListener(this)
        val log = stringBuffer.toString()
        if(log.isNotEmpty()) {
            logWriter.saveLogs(devicePoolId, testBatchId, device.toDeviceInfo(), listOf(log))
        }
    }
    
    private fun StringBuffer.reset() {
        delete(0, length)
    }
}
