package com.malinskiy.marathon.exceptions

class ExceptionsReporterFactory {

    companion object {
        fun get(enabled: Boolean): ExceptionsReporter = if (enabled) {
            BugsnagExceptionsReporter();
        } else {
            NoopExceptionsReporter();
        }
    }
}
