package com.malinskiy.marathon.extension

internal fun String.withPrefix(prefix: String?): String {
    return if (prefix.isNullOrEmpty()) {
        this
    } else {
        "$prefix.$this"
    }
}
