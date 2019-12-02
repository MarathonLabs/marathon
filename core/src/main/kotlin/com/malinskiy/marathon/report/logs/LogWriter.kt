package com.malinskiy.marathon.report.logs

import com.malinskiy.marathon.execution.Attachment
import com.malinskiy.marathon.execution.AttachmentType
import com.malinskiy.marathon.io.AttachmentManager
import com.malinskiy.marathon.io.FileType

class LogWriter(private val attachmentManager: AttachmentManager) {

    fun saveLogs(logs: List<String>): Attachment =
        attachmentManager
            .createAttachment(
                FileType.LOG,
                AttachmentType.LOG
            )
            .apply { this.file.writeText(logs.joinToString("\n")) }

}
