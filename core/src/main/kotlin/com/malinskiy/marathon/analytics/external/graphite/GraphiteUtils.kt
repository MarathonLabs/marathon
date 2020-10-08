package com.malinskiy.marathon.analytics.external.graphite

internal fun String.withPrefix(prefix: String?): String {
    return if (prefix.isNullOrEmpty()) {
        this
    } else {
        "$prefix.$this"
    }
}
