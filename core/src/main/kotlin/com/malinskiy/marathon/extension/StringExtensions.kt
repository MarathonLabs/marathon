package com.malinskiy.marathon.extension

internal fun String.withPrefix(prefix: String?): String {
    return if (prefix.isNullOrEmpty()) {
        this
    } else {
        "$prefix.$this"
    }
}

fun String.safePathLength(): String {
    return if(length >= 128) {
        substring(0 until 128)
    } else this
}
