package com.malinskiy.marathon.report.logs

import com.malinskiy.marathon.execution.Attachment
import com.malinskiy.marathon.execution.AttachmentType
import com.malinskiy.marathon.io.AttachmentManager
import com.malinskiy.marathon.io.FileType
import com.malinskiy.marathon.test.Test

class LogWriter(private val attachmentManager: AttachmentManager) {

    fun saveLogs(test: Test, logs: List<String>): Attachment {
        return attachmentManager.createAttachment(FileType.LOG, AttachmentType.LOG).apply {
            this.file.writeText(logs.joinToString("\n"))
        }
    }

}
