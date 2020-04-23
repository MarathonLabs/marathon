package com.malinskiy.marathon.android.adam.log

/**
 * Log class that mirrors the API in main Android sources.
 *
 * Default behavior outputs the log to [System.out]. Use
 * [.addLogger] to redirect the log somewhere else.
 */
object Log {
    private val mSpaceLine = CharArray(72)

    init {
        /* prep for hex dump */
        var i = mSpaceLine.size - 1
        while (i >= 0) mSpaceLine[i--] = ' '
        mSpaceLine[3] = '0'
        mSpaceLine[2] = mSpaceLine[3]
        mSpaceLine[1] = mSpaceLine[2]
        mSpaceLine[0] = mSpaceLine[1]
        mSpaceLine[4] = '-'
    }

    /**
     * Log Level enum.
     */
    enum class LogLevel(
        /**
         * Returns the numerical value of the priority.
         */
        //$NON-NLS-1$
        val priority: Int,
        /**
         * Returns a non translated string representing the LogLevel.
         */
        val stringValue: String,
        /**
         * Returns the letter identifying the priority of the [LogLevel].
         */
        val priorityLetter: Char
    ) {
        VERBOSE(2, "verbose", 'V'),  //$NON-NLS-1$
        DEBUG(3, "debug", 'D'),  //$NON-NLS-1$
        INFO(4, "info", 'I'),  //$NON-NLS-1$
        WARN(5, "warn", 'W'),  //$NON-NLS-1$
        ERROR(6, "error", 'E'),  //$NON-NLS-1$
        ASSERT(7, "assert", 'A');

        companion object {

            /**
             * Returns the [LogLevel] enum matching the specified letter.
             * @param letter the letter matching a `LogLevel` enum
             * @return a `LogLevel` object or `null` if no match were found.
             */
            private fun getByLetter(letter: Char): LogLevel? {
                for (mode in values()) {
                    if (mode.priorityLetter == letter) {
                        return mode
                    }
                }
                return null
            }

            /**
             * Returns the [LogLevel] enum matching the specified letter.
             *
             *
             * The letter is passed as a [String] argument, but only the first character
             * is used.
             * @param letter the letter matching a `LogLevel` enum
             * @return a `LogLevel` object or `null` if no match were found.
             */
            fun getByLetterString(letter: String): LogLevel? {
                return if (letter.isNotEmpty()) {
                    getByLetter(letter[0])
                } else null
            }
        }

    }
}


