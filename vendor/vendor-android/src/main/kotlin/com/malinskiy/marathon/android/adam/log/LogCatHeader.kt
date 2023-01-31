package com.malinskiy.marathon.android.adam.log

import java.util.Objects

/**
 * Data class for message header information which gets reported by logcat.
 */
class LogCatHeader {
    val logLevel: Log.LogLevel
    val pid: Int
    val tid: Int
    val appName: String
    val tag: String
    val timestamp: String

    constructor(
        logLevel: Log.LogLevel,
        pid: Int,
        tid: Int,
        appName: String,
        tag: String,
        timestamp: String
    ) {
        this.logLevel = logLevel
        this.pid = pid
        this.tid = tid
        this.appName = appName
        this.tag = tag
        this.timestamp = timestamp
    }

    override fun equals(other: Any?): Boolean {
        if (other !is LogCatHeader) {
            return false
        }
        return (this.logLevel == other.logLevel &&
            pid == other.pid &&
            tid == other.tid &&
            appName == other.appName &&
            tag == other.tag &&
            timestamp == other.timestamp)
    }

    override fun hashCode(): Int {
        var hashCode = 17
        hashCode = 31 * hashCode + this.logLevel.hashCode()
        hashCode = 31 * hashCode + pid
        hashCode = 31 * hashCode + tid
        hashCode = 31 * hashCode + appName.hashCode()
        hashCode = 31 * hashCode + tag.hashCode()
        hashCode = 31 * hashCode + Objects.hashCode(timestamp)
        return hashCode
    }

    override fun toString(): String {
        return StringBuilder().apply {
            append(timestamp)
                .append(": ")
                .append(logLevel.priorityLetter)
                .append('/')
                .append(tag)
                .append('(')
                .append(pid)
                .append(')')
        }.toString()
    }
}
