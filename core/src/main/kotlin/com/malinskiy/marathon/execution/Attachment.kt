package com.malinskiy.marathon.execution

import com.malinskiy.marathon.io.FileType
import java.io.File

data class Attachment(
    val file: File,
    val type: AttachmentType,
    val fileType: FileType
)

enum class AttachmentType {
    SCREENSHOT,
    VIDEO,
    LOG;

    fun toMimeType() = when (this) {
        SCREENSHOT -> "image/gif"
        VIDEO -> "video/mp4"
        LOG -> "text/txt"
    }
}