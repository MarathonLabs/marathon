package com.malinskiy.marathon.vendor.junit4.executor.listener

import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.Attachment
import com.malinskiy.marathon.execution.AttachmentType
import com.malinskiy.marathon.report.attachment.AttachmentListener
import com.malinskiy.marathon.report.attachment.AttachmentProvider
import com.malinskiy.marathon.report.logs.LogWriter
import com.malinskiy.marathon.vendor.junit4.Junit4Device
import com.malinskiy.marathon.vendor.junit4.model.TestIdentifier

class LogListener(private val device: Junit4Device,
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

    override suspend fun testRunStarted(runName: String, testCount: Int) {
        device.addLogListener(this)
    }

    override suspend fun testStarted(test: TestIdentifier) {
        super.testStarted(test)
        stringBuffer.reset()
    }

    override suspend fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {
        val file = logWriter.saveLogs(test.toTest(), devicePoolId, testBatchId, device.toDeviceInfo(), listOf(stringBuffer.toString()))
        attachmentListeners.forEach { it.onAttachment(test.toTest(), Attachment(file, AttachmentType.LOG)) }
    }

    override suspend fun testRunEnded(elapsedTime: Long, runMetrics: Map<String, String>) {
        device.removeLogListener(this)
    }

    override suspend fun testRunFailed(errorMessage: String) {
        device.removeLogListener(this)
    }

    private fun StringBuffer.reset() {
        delete(0, length)
    }
}
