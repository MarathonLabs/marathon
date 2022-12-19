package com.malinskiy.marathon.execution.listener

import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.Attachment
import com.malinskiy.marathon.execution.AttachmentType
import com.malinskiy.marathon.report.attachment.AttachmentProvider
import com.malinskiy.marathon.report.attachment.AttachmentProviderDelegate
import com.malinskiy.marathon.report.logs.LogProducer
import com.malinskiy.marathon.report.logs.LogWriter
import com.malinskiy.marathon.test.Test

class LogListener(
    private val deviceInfo: DeviceInfo,
    private val logProducer: LogProducer,
    private val devicePoolId: DevicePoolId,
    private val testBatchId: String,
    private val logWriter: LogWriter,
    private val attachmentProvider: AttachmentProviderDelegate = AttachmentProviderDelegate()
) : TestRunListener, AttachmentProvider by attachmentProvider, LineListener {
    private val stringBuffer = StringBuffer(4096)

    override suspend fun onLine(line: String) {
        stringBuffer.appendLine(line)
    }

    override suspend fun beforeTestRun() {
        logProducer.addLineListener(this)
    }

    override suspend fun testStarted(test: Test) {
        stringBuffer.reset()
    }

    override suspend fun testEnded(test: Test) {
        val log = stringBuffer.toString()
        stringBuffer.reset()
        if (log.isNotEmpty()) {
            val file = logWriter.saveLogs(test, devicePoolId, testBatchId, deviceInfo, listOf(log))
            attachmentProvider.onAttachment(test, Attachment(file, AttachmentType.LOG))
        }
    }

    override suspend fun afterTestRun() {
        logProducer.removeLineListener(this)
        val log = stringBuffer.toString()
        if (log.isNotEmpty()) {
            logWriter.saveLogs(devicePoolId, testBatchId, deviceInfo, listOf(log))
        }
    }

    private fun StringBuffer.reset() {
        delete(0, length)
    }
}
