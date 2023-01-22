package com.malinskiy.marathon.report.attachment

import com.malinskiy.marathon.execution.Attachment
import com.malinskiy.marathon.test.Test

class AttachmentProviderDelegate : AttachmentProvider {
    private val attachmentListeners = mutableListOf<AttachmentListener>()

    override fun registerListener(listener: AttachmentListener) {
        attachmentListeners.add(listener)
    }

    fun onAttachment(test: Test, attachment: Attachment) {
        attachmentListeners.forEach { it.onAttachment(test, attachment) }
    }
}
