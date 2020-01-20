package com.malinskiy.marathon.android.executor.listeners

import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.executor.listeners.line.LineListener
import com.malinskiy.marathon.report.attachment.AttachmentListener
import com.malinskiy.marathon.report.attachment.AttachmentProvider
import com.malinskiy.marathon.report.logs.LogWriter
import com.malinskiy.marathon.test.Test

class LogCatListener(
    private val device: AndroidDevice,
    private val logWriter: LogWriter
) : TestRunListener, AttachmentProvider, LineListener {
    private val attachmentListeners = mutableListOf<AttachmentListener>()

    override fun registerListener(listener: AttachmentListener) {
        attachmentListeners.add(listener)
    }

    private val stringBuffer = StringBuffer()

    override fun onLine(line: String) {
        stringBuffer.appendln(line)
    }

    override fun testRunStarted(runName: String, testCount: Int) {
        device.addLogcatListener(this)
    }

    override fun testEnded(test: Test, testMetrics: Map<String, String>) {
        device.removeLogcatListener(this)

        val attachment = logWriter.saveLogs(test, listOf(stringBuffer.toString()))

        attachmentListeners.forEach { it.onAttachment(test, attachment) }
    }

    override fun testRunEnded(elapsedTime: Long, runMetrics: Map<String, String>) {
        device.removeLogcatListener(this)
    }
}
