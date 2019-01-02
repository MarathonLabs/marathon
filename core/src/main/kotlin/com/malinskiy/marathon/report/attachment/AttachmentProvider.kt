package com.malinskiy.marathon.report.attachment

import com.malinskiy.marathon.execution.Attachment
import com.malinskiy.marathon.test.Test

interface AttachmentProvider {
    fun registerListener(listener: AttachmentListener)
}

interface AttachmentListener {
    fun onAttachment(test: Test, attachment: Attachment)
}