package com.malinskiy.marathon.execution

import java.io.File

data class Attachment(val file: File, val type: AttachmentType, val name: String? = null) {
    val empty: Boolean
        get() = !file.exists() || file.length() == 0L
}

enum class AttachmentType(val mimeType: String) {
    SCREENSHOT_GIF("image/gif"),
    SCREENSHOT_JPEG("image/jpeg"),
    SCREENSHOT_PNG("image/png"),
    SCREENSHOT_WEBP("image/webp"),
    VIDEO("video/mp4"),
    LOG("text/plain");
}
