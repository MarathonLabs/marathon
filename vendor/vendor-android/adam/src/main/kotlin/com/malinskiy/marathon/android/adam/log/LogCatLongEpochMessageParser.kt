package com.malinskiy.marathon.android.adam.log

import com.malinskiy.marathon.android.AndroidDevice
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAccessor
import java.util.*
import java.util.regex.Pattern

/* Parses the headers output by adb logcat -v long -v epoch. */
class LogCatLongEpochMessageParser : LogCatMessageParser() {
    override fun processLogHeader(string: String, device: AndroidDevice): LogCatHeader? {
        val matcher = HEADER.matcher(string)
        if (!matcher.matches()) {
            return null
        }
        val timestamp =
            EPOCH_TIME_FORMATTER.parse(
                matcher.group(1)
            ) { temporal: TemporalAccessor? ->
                Instant.from(
                    temporal
                )
            }
        val processId: Int = parseProcessId(matcher.group(2))
        val threadId: Int = parseThreadId(matcher.group(3))
        val priority: Log.LogLevel = parsePriority(matcher.group(4))
        val tag = matcher.group(5)
        previousHeader = LogCatHeader(
            priority,
            processId,
            threadId,
            getPackageName(processId, device),
            tag,
            timestamp
        )
        return previousHeader
    }

    companion object {
        val EPOCH_TIME: Pattern = Pattern.compile("\\d+\\.\\d\\d\\d")
        private val HEADER = Pattern.compile(
            "^\\[ +("
                + EPOCH_TIME
                + ") +("
                + PROCESS_ID
                + "): *("
                + THREAD_ID
                + ") ("
                + PRIORITY
                + ")/("
                + TAG
                + ") +]$"
        )
        val EPOCH_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatterBuilder()
            .appendValue(ChronoField.INSTANT_SECONDS)
            .appendFraction(ChronoField.MILLI_OF_SECOND, 3, 3, true)
            .toFormatter(Locale.ROOT)
    }
}
