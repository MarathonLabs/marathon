package com.malinskiy.marathon.android.adam.log

import java.time.Instant
import java.util.*

/**
 * Data class for message header information which gets reported by logcat.
 */
class LogCatHeader {
    val logLevel: Log.LogLevel
    val pid: Int
    val tid: Int
    val appName: String
    val tag: String
    val timestampInstant: Instant?
    private val mTimestamp: LogCatTimestamp?

    constructor(
        logLevel: Log.LogLevel,
        pid: Int,
        tid: Int,
        appName: String,
        tag: String,
        timestampInstant: Instant
    ) {
        this.logLevel = logLevel
        this.pid = pid
        this.tid = tid
        this.appName = appName
        this.tag = tag
        this.timestampInstant = timestampInstant
        mTimestamp = null
    }

    /**
     * Construct an immutable log message object.
     *
     */
    @Deprecated("Use {@link #LogCatHeader(LogLevel, int, int, String, String, Instant)}")
    constructor(
        logLevel: Log.LogLevel,
        pid: Int,
        tid: Int,
        appName: String,
        tag: String,
        timestamp: LogCatTimestamp
    ) {
        this.logLevel = logLevel
        this.pid = pid
        this.tid = tid
        this.appName = appName
        this.tag = tag
        timestampInstant = null
        mTimestamp = timestamp
    }

    @get:Deprecated(
        """Construct a LogCatHeader instance with {@link #LogCatHeader(LogLevel, int, int,
     *     String, String, Instant)} and use {@link #getTimestampInstant()}"""
    )
    val timestamp: LogCatTimestamp?
        get() = mTimestamp

    override fun equals(`object`: Any?): Boolean {
        if (`object` !is LogCatHeader) {
            return false
        }
        return (this.logLevel == `object`.logLevel &&
            pid == `object`.pid &&
            tid == `object`.tid &&
            appName == `object`.appName &&
            tag == `object`.tag &&
            timestampInstant == `object`.timestampInstant &&
            mTimestamp == `object`.mTimestamp)
    }

    override fun hashCode(): Int {
        var hashCode = 17
        hashCode = 31 * hashCode + this.logLevel.hashCode()
        hashCode = 31 * hashCode + pid
        hashCode = 31 * hashCode + tid
        hashCode = 31 * hashCode + appName.hashCode()
        hashCode = 31 * hashCode + tag.hashCode()
        hashCode = 31 * hashCode + Objects.hashCode(timestampInstant)
        hashCode = 31 * hashCode + Objects.hashCode(mTimestamp)
        return hashCode
    }

    override fun toString(): String {
        val builder = StringBuilder()
        if (timestampInstant == null) {
            builder.append(mTimestamp)
        } else {
            LogCatLongEpochMessageParser.EPOCH_TIME_FORMATTER.formatTo(timestampInstant, builder)
        }
        builder.append(": ")
            .append(this.logLevel.priorityLetter)
            .append('/')
            .append(tag)
            .append('(')
            .append(pid)
            .append(')')
        return builder.toString()
    }
}
