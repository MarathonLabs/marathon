package com.malinskiy.marathon.report.attachment

import com.malinskiy.marathon.execution.Attachment
import com.malinskiy.marathon.test.Test

class AttachmentCollector(attachmentProviders: List<AttachmentProvider>) : AttachmentListener {
    init {
        attachmentProviders.forEach {
            it.registerListener(this)
        }
    }
    
    private val attachments: MutableMap<Test, MutableList<Attachment>> = mutableMapOf()
    override fun onAttachment(test: Test, attachment: Attachment) {
        val list = attachments[test]
        if (list == null) {
            attachments[test] = mutableListOf(attachment)
        } else {
            list.add(attachment)
        }
    }

    operator fun get(test: Test): MutableList<Attachment>? {
        return attachments[test]
    }
}
