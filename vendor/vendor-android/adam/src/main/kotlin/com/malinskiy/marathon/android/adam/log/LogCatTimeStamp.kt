package com.malinskiy.marathon.android.adam.log

import java.util.regex.Pattern

/**
 * Data class for timestamp information which gets reported by logcat.
 */
data class LogCatTimestamp(
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    val second: Int,
    val milli: Int
) {
    override fun toString(): String {
        return String.format(
            "%02d-%02d %02d:%02d:%02d.%03d", month, day, hour, minute, second,
            milli
        )
    }

    companion object {
        private val timePattern = Pattern.compile(
            "^(\\d\\d)-(\\d\\d)\\s(\\d\\d):(\\d\\d):(\\d\\d)\\.(\\d+)$"
        )

        fun fromString(timeString: String): LogCatTimestamp {
            val matcher = timePattern.matcher(timeString)
            require(matcher.matches()) { "Invalid timestamp. Expected MM-DD HH:MM:SS:mmm" }
            val month = matcher.group(1).toInt()
            val day = matcher.group(2).toInt()
            val hour = matcher.group(3).toInt()
            val minute = matcher.group(4).toInt()
            val second = matcher.group(5).toInt()
            var millisecond = matcher.group(6).toInt()

            // ms is 3 digits max. e.g. convert "123456" into "123" (and rounding error is fine)
            while (millisecond >= 1000) {
                millisecond /= 10
            }
            return LogCatTimestamp(month, day, hour, minute, second, millisecond)
        }
    }

    /**
     * Construct an immutable timestamp object.
     */
    init {
        require(!(month < 1 || month > 12)) { String.format("Month should be between 1-12: %d", month) }
        require(!(day < 1 || day > 31)) { String.format("Day should be between 1-31: %d", day) }
        require(!(hour < 0 || hour > 23)) { String.format("Hour should be between 0-23: %d", hour) }
        require(!(minute < 0 || minute > 59)) { String.format("Minute should be between 0-59: %d", minute) }
        require(!(second < 0 || second > 59)) { String.format("Second should be between 0-59 %d", second) }
        require(!(milli < 0 || milli > 999)) { String.format("Millisecond should be between 0-999: %d", milli) }
    }
}
