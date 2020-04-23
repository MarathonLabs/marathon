package com.malinskiy.marathon.android.adam.log

import com.malinskiy.marathon.android.AndroidDevice
import java.util.regex.Pattern

/**
 * Class to parse raw output of { @code adb logcat -v long } to { @link LogCatMessage } objects.
 */
open class LogCatMessageParser {
    var previousHeader: LogCatHeader? = null

    /**
     * Parse a header line into a [LogCatHeader] object, or `null` if the input line
     * doesn't match the expected format.
     *
     * @param line   raw text that should be the header line from logcat -v long
     * @param device device from which these log messages have been received
     * @return a [LogCatHeader] which represents the passed in text
     */
    open fun processLogHeader(line: String, device: AndroidDevice): LogCatHeader? {
        val matcher = HEADER.matcher(line)
        if (!matcher.matches()) {
            return null
        }
        val dateTime: LogCatTimestamp = LogCatTimestamp.fromString(matcher.group(1))
        val processId = parseProcessId(matcher.group(2))
        val threadId = parseThreadId(matcher.group(3))
        val priority: Log.LogLevel = parsePriority(matcher.group(4))
        val tag = matcher.group(5)
        previousHeader = LogCatHeader(
            priority,
            processId,
            threadId,
            getPackageName(processId, device),
            tag,
            dateTime
        )
        return previousHeader
    }

    /**
     * Parse a list of strings into [LogCatMessage] objects. This method maintains state from
     * previous calls regarding the last seen header of logcat messages.
     *
     * @param lines  list of raw strings obtained from logcat -v long
     * @param device device from which these log messages have been received
     * @return list of LogMessage objects parsed from the input
     * @throws IllegalStateException if given text before ever parsing a header
     */
    fun processLogLines(
        lines: List<String>,
        device: AndroidDevice
    ): List<LogCatMessage> {
        val messages: MutableList<LogCatMessage> = ArrayList(lines.size)
        for (line in lines) {
            if (line.isEmpty()) {
                continue
            }
            if (processLogHeader(line, device) == null) {
                // If not a header line, this is a message line
                val prevHeader = previousHeader
                    ?: // If we are fed a log line without a header, there's nothing we can do with
                    // it - the header metadata is very important! So, we have no choice but to drop
                    // this line.
                    //
                    // This should rarely happen, if ever - for example, perhaps we're running over
                    // old logs where some earlier lines have been truncated.
                    continue
                messages.add(LogCatMessage(prevHeader, line))
            }
        }
        return messages
    }

    companion object {
        private val DATE_TIME = Pattern.compile("\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d\\.\\d\\d\\d")
        val PROCESS_ID = Pattern.compile("\\d+")
        val THREAD_ID = Pattern.compile("\\w+")
        val PRIORITY = Pattern.compile("[VDIWEAF]")
        val TAG = Pattern.compile(".*?")

        /**
         * Pattern for logcat -v long header ([ MM-DD HH:MM:SS.mmm PID:TID LEVEL/TAG ]). Example:
         *
         * <pre>[ 08-18 16:39:11.760  2977: 2988 D/PhoneInterfaceManager ]</pre>
         *
         *
         * Group 1: Date + Time<br></br>
         * Group 2: PID<br></br>
         * Group 3: TID (hex on some systems!)<br></br>
         * Group 4: Log Level character<br></br>
         * Group 5: Tag
         */
        private val HEADER = Pattern.compile(
            "^\\[ ("
                + DATE_TIME
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

        fun parseProcessId(string: String): Int {
            return try {
                string.toInt()
            } catch (exception: NumberFormatException) {
                -1
            }
        }

        fun parseThreadId(string: String): Int {
            return try {
                // Some versions of logcat return hexadecimal thread IDs. Propagate them as decimal.
                Integer.decode(string)
            } catch (exception: NumberFormatException) {
                -1
            }
        }

        /**
         * Parses the [priority] part of a logcat message header:](https://developer.android.com/studio/command-line/logcat.html) the "I" in
         *
         * <pre>[          1517949446.554  2848: 2848 I/MainActivity ]</pre>
         *
         * @return the log level corresponding to the priority. If the argument is not one of the
         * expected letters returns LogLevel.WARN.
         */
        fun parsePriority(string: String): Log.LogLevel {
            return Log.LogLevel.getByLetterString(string)
                ?: return if (string != "F") {
                    Log.LogLevel.WARN
                } else Log.LogLevel.ASSERT
        }

        fun getPackageName(processId: Int, device: AndroidDevice): String {
            //TODO: Figure out if it's doable with adam
            return "?"
        }
    }
}
