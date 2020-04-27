package com.malinskiy.marathon.android.adam.log

/**
 * Model a single log message output from `logcat -v long`.
 *
 * Every message is furthermore associated with a [LogCatHeader] which contains additionally
 * meta information about the message.
 */
data class LogCatMessage(private val header: LogCatHeader, val message: String) {
    val logLevel: Log.LogLevel
        get() = header.logLevel

    val pid: Int
        get() = header.pid

    val tid: Int
        get() = header.tid

    val appName: String
        get() = header.appName

    val tag: String
        get() = header.tag

    val timestamp: String
        get() = header.timestamp

    override fun toString() = "$header: $message"

}
